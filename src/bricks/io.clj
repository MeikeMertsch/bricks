(ns bricks.io
  (:require [clojure.java.io :as io]
            [bricks.color :as color]
            [bricks.conversion :as conv]
            [bricks.tmp :as tmp]))

(defn parse-lines-with-f [file-name f]
  (let [reader (io/reader file-name)]
    (->> (line-seq reader)
         (map f))))

(defn lot-in-set? [set instructions]
  (empty? (tmp/find-in set (conv/->item instructions))))

(defn log-line [line e]
  [(format "%s --> skipped: %s" line e)])

(defn write-lines [file-path lines]
  (with-open [wtr (clojure.java.io/writer (str file-path "_changed"))]
    (doseq [line lines] (.write wtr (str line "\n")))))

(defn parse-updates-in [set line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part (conv/->int qty) (color/name->id color)]]
        (if (not (lot-in-set? set instructions))
          instructions
          (throw (Exception. "Lot not in set!"))))
      (catch Exception e
        (log-line line e)))))

(defn parse-additions-in [set line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part (conv/->int qty) (color/name->id color)]]
        (if (lot-in-set? set instructions)
          instructions
          (throw (Exception. "Lot already in set!"))))
      (catch Exception e
        (log-line line e)))))

(defn parse-deletions-in [set line]
  (let [[part color] (clojure.string/split line #";")]
    (try
      (let [instructions [line part 0 (color/name->id color)]]
        (if (not (lot-in-set? set instructions))
          [line part (color/name->id color)]
          (throw (Exception. "Lot not in set!"))))
      (catch Exception e
        (log-line line e)))))