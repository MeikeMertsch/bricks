(ns bricks.sets
  (:require [com.rpl.specter :as specter]
            [bricks.io :as io]
            [bricks.conversion :as conv]
            [bricks.tmp :as tmp]
            [bricks.api :as api]))

(defn validate-instructions
  ([log] [log])
  ([line part quantity color-id]
   (let [log #(format "%s --> skipped: %s" line %)]
     (try
       (if (api/known-color? part color-id)
         [line part quantity color-id]
         [(log "color is not known for that part")])
       (catch Exception e
         [(log e)])))))

(defn multiply-set [set times]
  (map (fn [item] (update-in item [:quantity] #(* times (conv/->int %)))) set))


(defn delete-in-set [set instructions]
  (loop [instructions instructions
         set set]
    (if (empty? instructions)
      set
      (recur (rest instructions)
             (let [[_ part color-id] (first instructions)]
               (remove (fn [item] (and (= color-id (:color_id item))
                                       (= part (:no (:item item))))) set))))))

(defn update-in-set [set instructions]
  (loop [instructions instructions
         set set]
    (if (empty? instructions)
      set
      (recur (rest instructions)
             (let [[_ part qty color-id] (first instructions)
                   selector [specter/ALL (fn [item] (and (= color-id (:color_id item))
                                                         (= part (:no (:item item))))) :quantity]]
               (specter/setval selector qty set))))))

(defn all-valid? [instructions]
  (empty? (filter (fn [item] (= 1 (count item))) instructions)))

(defn add-in-set [set instructions]
  (->> (map #(apply validate-instructions %) instructions)
       (#(if (all-valid? %)
          (->> (map conv/->item %)
               (concat set))
          (let [error-file (format "error_%s" (System/currentTimeMillis))]
            (io/write-lines error-file (map first %)) ; -> error namespace
            (throw (Exception. (format "Non-valid additions detected. See %s" error-file))))))))

(defn count-parts [set]
  (reduce (fn [sum {qty :quantity}] (+ sum (conv/->int qty))) 0 set))

(defn deal-with-duplicates [update-instructions]
  (->> (group-by (fn [[_ part _ color-id]] [part color-id]) update-instructions)
       (mapcat (fn [[_ coll]] (if (= 1 (count (first coll)))
                                coll
                                [(reduce (fn [[line part qty_a color-id] [_ _ qty_b _]]
                                           [line part (+ qty_a qty_b) color-id])
                                         coll)])))))

(defn check-inventory [parts inventory]
  (loop [set parts
         result []]
    (if (empty? set)
      result
      (recur (rest set)
             (let [item (first set)]
               (conj result (conj (tmp/find-in inventory item) item)))))))
