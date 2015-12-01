(ns bricks.io
  (:require [clojure.java.io :as io]))

(defn parse-lines-with-f [file-name f]
  (let [reader (io/reader file-name)]
    (->> (line-seq reader)
         (map f))))

(defn write-lines [file-path lines]
  (with-open [wtr (clojure.java.io/writer (str file-path "_changed"))]
    (doseq [line lines] (.write wtr (str line "\n")))))

