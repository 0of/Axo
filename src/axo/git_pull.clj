(ns axo.git-pull
 (:import [java.io FileNotFoundException File
           [org.eclipse.jgit.lib RepositoryBuilder AnyObjectId]
           [org.eclipse.jgit.api Git InitCommand StatusCommand AddCommand
            ListBranchCommand PullCommand MergeCommand LogCommand
            LsRemoteCommand Status ResetCommand$ResetType
            FetchCommand]
           [org.eclipse.jgit.submodule SubmoduleWalk]
           [com.jcraft.jsch Session JSch]
           [org.eclipse.jgit.transport FetchResult JschConfigSessionFactory
            OpenSshConfig$Host SshSessionFactory]
           [org.eclipse.jgit.util FS]
           [org.eclipse.jgit.merge MergeStrategy]
           [clojure.lang Keyword]
           [java.util List]
           [org.eclipse.jgit.api.errors JGitInternalException]
           [org.eclipse.jgit.transport UsernamePasswordCredentialsProvider]
           [org.eclipse.jgit.treewalk TreeWalk]]))

(defn load-repo
  "Given a path (either to the parent folder or to the `.git` folder itself), load the Git repository"
  ^org.eclipse.jgit.api.Git [path]
  (if-let [git-dir (discover-repo path)]
    (-> (RepositoryBuilder.)
        (.setGitDir git-dir)
        (.readEnvironment)
        (.findGitDir)
        (.build)
        (Git.))
    (throw
     (FileNotFoundException. (str "The Git repository at '" path "' could not be located.")))))

(defn git-clone
  ([uri
     (git-clone uri (util/name-from-uri uri) "origin" "master" false)])
  ([uri local-dir remote-name local-branch bare?
     (-> (clone-cmd uri)
         (.setDirectory (io/as-file local-dir))
         (.setRemote remote-name)
         (.setBranch local-branch)
         (.setBare bare?)
         (.call))]))

(defn git-pull
  [])