(ns bricks.io
  (:require [clojure.java.io :as io]
            [bricks.color :as color]
            [bricks.conversion :as conv]))

(defn parse-lines-with-f [file-name f]
  (let [reader (io/reader file-name)]
    (->> (line-seq reader)
         (map f))))

(defn write-lines [file-path lines]
  (with-open [wtr (clojure.java.io/writer (str file-path "_changed"))]
    (doseq [line lines] (.write wtr (str line "\n")))))

(defn parse-upload-instructions [line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      [line part (conv/->int qty) (color/color-id color)]

      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))

(defn parse-deletions [line]
  (let [[part color] (clojure.string/split line #";")]
    (try
      [line part (color/color-id color)]

      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))