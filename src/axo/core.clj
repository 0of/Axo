(ns axo.core
  (:require [aleph.http :as http]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [axo.app-config :as config]
            [ring.util.response :refer [resource-response header]]))  

(defroutes app
  ;; main page
  (GET  "/app" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/"))

(def handlers
  (-> (handler/site app)
      config/api-config-handler))

(defn -main
  [& args]
  (config/)
  (http/start-server handlers {:port 8000}))