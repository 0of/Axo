(ns axo.git-access
   (:require [clojure.java.io :as io])   
   (:import [org.eclipse.jgit.lib RepositoryBuilder BatchingProgressMonitor]
            [org.eclipse.jgit.api Git CloneCommand PullCommand]
            [org.apache.commons.io FileUtils]))

(defprotocol GitProgressMonitor
  (onUpdate [this session percent]))

(defn GitBatchProgressMonitor
  [monitor]
  (proxy [BatchingProgressMonitor] []
    (onEndTask 
      ([task-name _])
      ([task-name work-curr total percent])) 
    (onUpdate 
      ([task-name work-curr total percent])
      ([task-name work-curr]))))

(defn get-repo-absolute-path
  ^String 
  [^Git git-repo]
  (-> git-repo
      (.getRepository)
      (.getDirectory)
      (.getAbsolutePath)))

(defn git-load
  [git-dir]
  (-> (RepositoryBuilder.)
      (.setGitDir git-dir)
      (.readEnvironment)
      (.findGitDir)
      (.build)
      (Git.)))

(defn git-clone
  [uri local-dir remote-name local-branch bare? monitor]
  (-> (Git/cloneRepository)
      (.setProgressMonitor (GitBatchProgressMonitor monitor))
      (.setURI uri)
      (.setDirectory (io/as-file local-dir))
      (.setRemote remote-name)
      (.setBranch local-branch)
      (.setBare bare?)
      (.call)))

(defn git-pull
 [^Git git-repo]
 (-> git-repo
     (.pull)
     (.call)))

(defn git-delete
 [^Git git-repo]
 (let [resp (-> git-repo
                (.getRepository))
       resp-dir (.getDirectory resp)]
    (.close resp)
    (FileUtils/deleteDirectory resp-dir)))
