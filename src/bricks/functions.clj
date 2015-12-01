(ns bricks.functions
  (:require [bricks.color :as color]
            [bricks.html :as html]
            [bricks.io :as io]
            [cheshire.core :as json]))

(defn ->int [string]
  (int (bigint string)))

(defn avg_price [number color-id]
  (->> (html/html-get (str "/items/part/" number "/price")
                      {:type       "part"
                       :no         number
                       :color_id   color-id
                       :guide_type "sold"})
       :avg_price))

(defn parse-upload-instructions [line]
  (let [[part qty color] (clojure.string/split line #";")]
    (try
      [line part (->int qty) (color/color-id color)]

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

(defn ->items [[_ part quantity color-id]]
  {:item           {:no   part
                    :type "part"}
   :color_id       color-id
   :quantity       quantity
   :unit_price     (avg_price part color-id)
   :new_or_used    "N"
   :description    ""
   :remarks        ""
   :bulk           1
   :is_retain      false
   :is_stock_room  true
   :stock_room_id  "B"
   :my_cost        0.0
   :sale_rate      0
   :tier_quantity1 0
   :tier_price1    0
   :tier_quantity2 0
   :tier_price2    0
   :tier_quantity3 0
   :tier_price3    0})


(defn part-out [set-no]
  (->> (html/html-get (format "/items/set/%s/subsets" set-no)
                      {:type          "set"
                       :no            set-no
                       :instruction   true
                       :break_subsets true})
       (map #(get-in % [:entries 0]))))



(defn multiply-set [set times]
  (map (fn [item] (update-in item [:quantity] #(* times (->int %)))) set))

(defn delete-in-set [set instructions]
  (loop [instructions instructions
         set set]
    (if (empty? instructions)
      set
      (recur (rest instructions)
             (let [[_ part color-id] (first instructions)]
               (remove (fn [item] (and (= color-id (:color_id item))
                                       (= part (:no (:item item))))) set))))))


(defn upload-inventories [file]
  (->> parse-upload-instructions
       (io/parse-lines-with-f file)
       (map #(apply validate-instructions %))
       (#(if (empty? (filter (fn [item] (= 1 (count item))) %))
          (->> (map ->items %)
               ((fn [x] (html/html-post "/inventories" x)))
               println)
          (io/write-lines file (map first %))))))

(defn part-out-set [set-no quantity delete-file update-file]
  (let [inventory (multiply-set (part-out set-no) quantity)
        deletions (io/parse-lines-with-f delete-file parse-deletions)
        updates (io/parse-lines-with-f update-file parse-upload-instructions)]
    (delete-in-set inventory deletions)
    
    ; Load set inventory
    ; Multiply by quantity

    ; Parse Delete Instructions
    ; Parse Update Instructions

    ; Delete from set inventory
    ; Update in set inventory

    ; Set Prices

    ; Create upload instructions
    ; POST
    ))

; TODO:
(defn update-inventories [file stockroom]
  ; Load file
  ; Parse Instructions
  ; Get Inventories in chosen stockroom
  ; get Item by item-no & color_id
  ; create update instructions per item
  ; PUT
  )

; TODO:
(defn delete-inventories [file stockroom]
  ; Load file
  ; Parse Instructions
  ; Get Inventories in chosen stockroom
  ; get Item by item-no & color_id
  ; create delete instructions per item
  ; DELETE
  )

; TODO:
(defn change-pricing [sum-costs stockroom]
  ; Get Inventories in chosen stockroom
  ; Calculate Sum of Prices of parts
  ; create Update instructions per Item
  ; PUT
  )



