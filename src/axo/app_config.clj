(ns axo.app-config
   (:require [clojure.java.io :as io]   
             [clojure.edn :as edn])
   (:import [java.io IOException]
            [org.apache.commons.io FileUtils FilenameUtils]))

(defn- default-config
 []
 {:repo-dir (FileUtils/getUserDirectoryPath)})

(def value (atom (default-config)))

(defn- write-config
 [file-path]
 (with-open [wr (io/writer file-path)]
   (.write wr (prn-str @value))))

(defn- read-config
 [file-path]
 (let [config (edn/read (slurp file-path))]
   ;; validate config
   (if (:repo-dir config)
     (reset! value config))))

(defn app-config-handler
  [f]
  (fn [request]
    (f (assoc request :app-config @value))))

(defn init-config
 [^String app-dir]
 (let [file-path (FilenameUtils/concat app-dir "config.edn")]
    (try
      (read-config file-path)
      (catch IOException e
        (write-config file-path)))))

(defn modify-config
  [{app-dir :app-dir} new-config]
  (let [file-path (FilenameUtils/concat app-dir "config.edn")]
    (reset! value new-config)
    (write-config file-path)  
    [:ok nil])) 