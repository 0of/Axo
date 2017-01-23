(ns axo.text-search
  (:require [clojure.java.io :as io]) 
  (:import [org.apache.lucene.store Directory NIOFSDirectory]
           [org.apache.lucene.analysis Analyzer TokenStream]
           [org.apache.lucene.analysis.standard StandardAnalyzer]
           [org.apache.lucene.index IndexWriter IndexReader DirectoryReader IndexWriterConfig Term]
           [org.apache.lucene.search FuzzyQuery IndexSearcher TopDocs]
           [org.apache.lucene.document Document TextField Field$Store]))

(defn- directory-storage
  ^Directory 
  [path]
  (NIOFSDirectory. (io/as-file path)))

(defn index-writer
  ^IndexWriter
  [path]
  (let [dir (directory-storage path)]
    (IndexWriter. dir (IndexWriterConfig. (StandardAnalyzer.)))))

(defn index-reader
  ^IndexReader
  [path]
  (let [dir (directory-storage path)]
    (DirectoryReader/open dir)))

(defn store
  [writer kvs]
  (let [document (Document.)]
    (doseq [[key {:keys [value meta]}] kvs]
      (let [field (TextField. key value Field$Store/YES)]
        (.add ^Document document field)))

    (.addDocument writer document)))

(defn search
  [reader field q limit]
  (let [searcher (IndexSearcher. reader)
        term (Term. field q)
        query (FuzzyQuery. term)
        hits (.search searcher query limit)]
    (dotimes [i (alength (.-scoreDocs hits))]
      (aget (.-scoreDocs hits) i))))