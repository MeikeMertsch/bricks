(ns bricks.io
  (:require [clojure.java.io :as io]
            [bricks.color :as color]
            [bricks.conversion :as conv]
            [bricks.tmp :as tmp]))

(defn parse-lines-with-f [file-name f]
  (let [reader (io/reader file-name)]
    (->> (line-seq reader)
         (map f))))

(defn write-lines [file-path lines]
  (with-open [wtr (clojure.java.io/writer (str file-path "_changed"))]
    (doseq [line lines] (.write wtr (str line "\n")))))

(defn parse-updates-in [set line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part (conv/->int qty) (color/color-id color)]]
        (if (not (empty? (tmp/find-in set (conv/->item instructions))))
          instructions
          (throw (Exception. "Lot not in set!"))))
      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))

(defn parse-additions-in [set line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part (conv/->int qty) (color/color-id color)]]
        (if (empty? (tmp/find-in set (conv/->item instructions)))
          instructions
          (throw (Exception. "Lot already in set!"))))
      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))

(defn parse-deletions-in [set line]
  (let [[part color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part 0 (color/color-id color)]]
        (if (not (empty? (tmp/find-in set (conv/->item instructions))))
          [line part (color/color-id color)]
          (throw (Exception. "Lot not in set!"))))
      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))