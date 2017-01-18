(ns axo.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :refer [resource-response header]]))  

(defroutes app
  ;; main page
  (GET  "/app" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/"))

(defn -main
  [& args]
  (jetty/run-jetty app {:port 8000}))