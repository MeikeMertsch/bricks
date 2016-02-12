(ns bricks.io
  (:require [clojure.java.io :as io]))

(defn read-with-parser [file-name parse-function]
  (let [reader (io/reader file-name)]
    (->> (line-seq reader)
         (map parse-function))))

(defn write-lines [file-path lines]
  (with-open [wtr (clojure.java.io/writer file-path)]
    (doseq [line lines] (.write wtr (str line "\n")))))