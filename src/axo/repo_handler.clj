(ns axo.repo-handler
  (:require [axo.git-access :as git]
            [clj-leveldb :as db]
            [clojure.core.async :refer [go]]
            [clojure.string :refer [split]])
  (:import [org.eclipse.jgit.api.errors GitAPIException InvalidRemoteException TransportException]
           [java.io IOException]
           [org.apache.commons.io FilenameUtils]))

(defn AddGitMonitor
  [channel]
  (reify git/GitProgressMonitor
    (onUpdate [this session percent])))
      ; (ws/send! channel {:session sessionc
      ;                    :progress percent}))))

(defn add-repo
  [{:keys [repos-db channel]} url]
  (if-let [local-path (db/get repos-db url)]
    [:error :already-existent]
    (let [name (last (split url #"/"))
          location (FilenameUtils/concat "/tmp" name)
          session (hash url)]
      (go
        (let [cloned-repo (git/git-clone url location "origin" "master" false (AddGitMonitor channel))]
          (db/put repos-db url (git/get-repo-absolute-path cloned-repo))))

      [:ok {:session session
            :state "started"}])))

(defn remove-repo
  [{:keys [repos-db]} url]
  (if-let [local-path (db/get repos-db url)]
    (let [git-repo (git/git-load local-path)]
      (git/git-delete git-repo)
      (db/delete repos-db url)
      [:ok nil])
    [:error :non-existent]))

(defn refresh-repo
  [{:keys [repos-db channel]} url]
  (if-let [local-path (db/get repos-db url)]
    (try
      (let [git-repo (git/git-load local-path)
            session (hash url)]
        (git/git-pull git-repo)
        [:ok {:session session
              :state "started"}])
      (catch IOException e
        [:error :io-exception]))
    [:error :non-existent]))

(defn list-repos
  [{:keys [repos-db]}]
  (let [result (db/iterator repos-db)]
    [:ok (map first result)]))

(defn load-repos-db
  [^String app-dir]
  (let [dat-dir (FilenameUtils/concat app-dir "dat")]
    (try
      [:ok (db/create-db dat-dir  {:key-decoder byte-streams/to-string 
                                   :val-decoder byte-streams/to-string})]
      (catch IOException e
        [:error :io-exception]))))