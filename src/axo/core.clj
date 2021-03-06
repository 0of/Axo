(ns axo.core
  (:require [ring.adapter.jetty9 :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [axo.app-config :as config]
            [axo.repo-handler :as repo]
            [ring.middleware.json :as json]
            [clojure.data.json :as json-parser]
            [ring.util.response :refer [resource-response header]]
            [bidi.bidi :as bidi])
  (:import [org.apache.commons.io FilenameUtils])
  (:gen-class))  

(defn- user-config
 [{:keys [app-dir body]}]
 (let [[status code] (config/modify-config {:app-dir app-dir} body)]
   (if (= :ok status)
     {:status 200}
     {:status 400 :body {:error (name code)}})))

(defn- user-repos
 [{:keys [repos-db]}] 
 (let [[status repos] (repo/list-repos {:repos-db repos-db})]
   (if (= :ok status)
     {:status 200 :body {:repos repos}}
     {:status 400 :body {:error (name repos)}})))

(defn- new-repo
 [{:keys [repos-db body]}]
 (let [[status result] (repo/add-repo {:repos-db repos-db
                                       :channel nil}
                         (:url body))]
    (if (= :ok status)
      {:status 200 :body nil}
      {:status 400 :body {:error (name result)}})))               

(defroutes app
  ;; main page
  (context "/app" []
    (GET  "/" [] (resource-response "index.html" {:root "public"}))

    (context "/user" []
      (PATCH "/config" req (user-config req))  

      (context "/repos" []  
        (GET "/" req (user-repos req))
        (POST "/" req (new-repo req)))))

  (route/resources "/"))

(def ws-app
  ["POST /app/user/repos" #(new-repo %)])

(defn- make-request
  [uri body ws]
  {:body body
   :uri uri
   :channel ws})

(defn- match-ws-handler
  [{body :body method :method} ws]
  (if-let [handler (bidi/match-route ws-app)]
    (handler (make-request method body ws))
    {:error "not found" :body ""}))

(defn app-dir-handler
  [f dir]
  (fn [request]
    (f (assoc request :app-dir dir))))

(defn app-repos-handler
  [f repos-db]
  (fn [request]
    (f (assoc request :repos-db repos-db))))

(defn get-handlers
  [app-dir repos-db]
  (-> (handler/site app)
      json/wrap-json-response
      (json/wrap-json-body {:keywords? true}) 
      config/app-config-handler
      (app-repos-handler repos-db)
      (app-dir-handler app-dir)))

(defn- find-jar
  []
  (-> (class *ns*)
      .getProtectionDomain 
      .getCodeSource 
      .getLocation
      .getPath))

(def channels (atom #{}))

(defn- general-channel-on-message 
  [ws raw-message]
  (let [msg (json-parser/read-str raw-message)]
    (if-let [method (:method msg)]
      (match-ws-handler msg)
      {:error "invalid format" :body ""})))

(defn- on-connect
  [channel]
  (swap! channels conj channel))

(defn- on-disconnect
  [channel] 
  (swap! channels disj channel))

(def channel-handler {:on-connect on-connect
                      :on-close on-disconnect
                      :on-text general-channel-on-message})

(defn -main
  [& args]
  (let [app-dir (FilenameUtils/getFullPath (find-jar))
        _ (config/init-config app-dir)
        [status repos-db] (repo/load-repos-db app-dir)]
    (jetty/run-jetty (get-handlers app-dir repos-db) {:port 8000
                                                      :websockets {"/app/channel" channel-handler}})))                                                       