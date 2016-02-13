(ns bricks.parse
  (:require [bricks.color :as color]
            [bricks.conversion :as conv]
            [bricks.tmp :as tmp]))

(defn lot-in-set? [set instructions]
  (empty? (tmp/find-in set (conv/->item instructions))))

(defn log-line [line e]
  [(format "%s --> skipped: %s" line e)])

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


(defn parse-confirmed [line]
  (let [[_ qty part color-name type] (take 5 (clojure.string/split line #";"))]
    (conv/->item [_ part (int (bigint qty)) (color/short-name->id color-name)] type)))