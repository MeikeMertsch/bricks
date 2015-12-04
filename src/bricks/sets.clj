(ns bricks.sets
  (:require [bricks.html :as html]
            [com.rpl.specter :as specter]
            [bricks.io :as io]
            [bricks.conversion :as conv]
            [bricks.color :as color]))

(defn validate-instructions
  ([log] [log])
  ([line part quantity color-id]
   (let [log #(format "%s --> skipped: %s" line %)]
     (try
       (if (color/known-color? part color-id)
         [line part quantity color-id]
         [(log "color is not known for that part")])
       (catch Exception e
         [(log e)])))))

(defn part-out [set-no]
  (->> (html/html-get (format "/items/set/%s/subsets" set-no)
                      {:type          "set"
                       :no            set-no
                       :instruction   true
                       :break_subsets true})
       (map #(get-in % [:entries 0]))))


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
            (io/write-lines error-file (map first %))
            (throw Exception (format "Non-valid additions detected. See %s" error-file)))))))

(defn count-parts [set]
  (reduce (fn [sum {qty :quantity}] (+ sum (conv/->int qty))) 0 set))

(defn find-in [inventory item]
  (filter #(and (= (:color_id item) (:color_id %))
                (= (:no (:item item)) (:no (:item %)))
                (= (:type (:item item)) (:type (:item %))))
          inventory))

(defn check-inventory [set inventory]
  set
  (loop [set set
         result []]
    (if (empty? set)
      result
      (recur (rest set)
             (let [item (first set)]
               (conj result (conj (find-in inventory item) item)))))))