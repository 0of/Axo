(ns axo.core
  (:require [aleph.http :as http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [axo.app-config :as config]
            [ring.util.response :refer [resource-response header]])
  (:import [org.apache.commons.io FilenameUtils])
  (:gen-class))  

(defroutes app
  ;; main page
  (GET  "/app" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/"))

(defn app-dir-handler
  [dir f]
  (fn [request]
    (f (assoc request :app-dir dir))))

(defn get-handlers
  [app-dir]  
  (-> (handler/site app)
      config/app-config-handler
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
  (let [app-dir ((FilenameUtils/getFullPath (find-jar)))]
    (config/init-config app-dir)
    (http/start-server (get-handlers app-dir) {:port 8000})))