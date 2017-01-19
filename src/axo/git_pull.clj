(ns axo.git-pull
   (:require [clojure.java.io :as io])   
   (:import [org.eclipse.jgit.lib RepositoryBuilder BatchingProgressMonitor]
            [org.eclipse.jgit.api Git CloneCommand PullCommand]))

(defn GitBatchProgressMonitor
  []
  (proxy [BatchingProgressMonitor] []
    (onEndTask 
      ([task-name _])
      ([task-name work-curr total percent])) 
    (onUpdate 
      ([task-name work-curr total percent])
      ([task-name work-curr])))) 

(defn git-load
  [git-dir]
  (-> (RepositoryBuilder.)
      (.setGitDir git-dir)
      (.readEnvironment)
      (.findGitDir)
      (.build)
      (Git.)))

(defn git-clone
  [uri local-dir remote-name local-branch bare?]
  (-> (Git/cloneRepository)
      (.setProgressMonitor (GitBatchProgressMonitor))
      (.setURI uri)
      (.setDirectory (io/as-file local-dir))
      (.setRemote remote-name)
      (.setBranch local-branch)
      (.setBare bare?)
      (.call)))


