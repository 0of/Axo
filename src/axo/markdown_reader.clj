(ns axo.markdown-reader
   (:require [clojure.java.io :as io])   
   (:import [com.vladsch.flexmark.parser Parser]
            [com.vladsch.flexmark.ast Heading]))            

(defn node-tree-seq
 [root]
 (let [walk (fn walk
              [level coll]
              (loop [lv level
                     c coll 
                     r []]
                (if-let [node (first c)]
                   (if (instance? Heading node)
                     (if (> (.getLevel node) lv)
                        (let [[children rest-coll] (walk (.getLevel node) (rest c))]
                          (recur lv rest-coll (conj r node children)))
                        [r c])
                     ;; normal node
                     (recur lv (rest c) (conj r node)))                    
                   [r c])))]
   (first (walk 0 (seq (.getChildren root))))))

(defn read-file
  [path]    
  (with-open [reader (io/reader path)]
    (let [parser (.build (Parser/builder))]
      (.parseReader parser reader))))