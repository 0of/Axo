(defproject src "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]     
                 [org.clojure/core.async "0.2.395"]          
                 [ring/ring-core "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [aleph "0.4.1"]
                 [compojure "1.5.1"]
                 [clj-jgit "0.8.9"]
                 [org.apache.directory.studio/org.apache.commons.io "2.4"]
                 [factual/clj-leveldb "0.1.1"]
                 [org.apache.lucene/lucene-core "6.3.0"] 
                 [com.vladsch.flexmark/flexmark "0.11.6"]
                 [org.omcljs/om "1.0.0-alpha34"]
                 [cljs-http "0.1.21"]]
  :main ^:skip-aot axo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}})
