(ns axo.core
  (:require [aleph.http :as http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [axo.app-config :as config]
            [axo.repo-handler :as repo]
            [ring.util.response :refer [resource-response header]])
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
 (let [[status repos] (repo/list-repos repos-db)]
   (if (= :ok status)
     {:status 200 :body {:repos repos}}
     {:status 400 :body {:error (name repos)}})))

(defroutes app
  ;; main page
  (context "/app" []
    (GET  "/" [] (resource-response "index.html" {:root "public"}))

    (context "/user" []
      (GET "/repos" req (user-repos req))
      (PATCH "/config" req (user-config req))))

  (route/resources "/"))

(defn app-dir-handler
  [dir f]
  (fn [request]
    (f (assoc request :app-dir dir))))

(defn app-repos-handler
  [repos-db f]
  (fn [request]
    (f (assoc request :repos-db repos-db))))

(defn get-handlers
  [app-dir repos-db]  
  (-> (handler/site app)
      config/app-config-handler
      (partial app-repos-handler repos-db)
      (partial app-dir-handler app-dir)))

(defn- find-jar
  []
  (-> (class *ns*)
      .getProtectionDomain 
      .getCodeSource 
      .getLocation
      .getPath))

(defn -main
  [& args]
  (let [app-dir ((FilenameUtils/getFullPath (find-jar)))
        _ (config/init-config app-dir)
        repos-db (repo/load-repos-db app-dir)]
    (http/start-server (get-handlers app-dir repos-db) {:port 8000})))