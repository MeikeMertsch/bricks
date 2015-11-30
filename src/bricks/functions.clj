(ns bricks.functions
  (:require [bricks.constants :as const]
            [bricks.html :as html]
            [bricks.io :as io]
            [cheshire.core :as json]
            [com.rpl.specter :as specter]))

(def colors (->> (slurp "resources/colors")
                 (#(json/parse-string % const/transform-to-keywords))))

(defn color-id [name]
  (let [colors-->id [specter/ALL #(= (clojure.string/lower-case name) (:color_name %)) :color_id]
        id (peek (specter/select colors-->id colors))]
    (if id id (throw (Exception. "The color isn't recognized")))))

(defn price [number color-id]
  (->> (html/html-get (str "/items/part/" number "/price")
                      {:type       "part"
                       :no         number
                       :color_id   color-id
                       :guide_type "sold"})
       :avg_price))



(defn parse-upload-instructions [line]
  (let [[part qty color] (clojure.string/split line #";")
        ->int #(int (bigint %))]
    (try
      [line part (->int qty) (color-id color)]

      (catch Exception e
        (let [log (format "%s --> skipped: %s" line e)]
          [log])))))

(defn known-color? [part color-id]
  (->> (html/html-get (str "/items/part/" part "/colors"))
       (filter #(= color-id (:color_id %)))
       ((complement empty?))))


(defn validate-instructions
  ([log] [log])
  ([line part quantity color-id]
   (let [log #(format "%s --> skipped: %s" line %)]
     (try
       (if (known-color? part color-id)
         [line part quantity color-id]
         [(log "color is not known for that part")])
       (catch Exception e
         [(log e)])))))


(defn ->items [[_ part quantity color-id]]
  {:item           {:no   part
                    :type "part"}
   :color_id       color-id
   :quantity       quantity
   :unit_price     (price part color-id)
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



(defn upload-inventories [file]
  (->> (io/read-lines file parse-upload-instructions)
       (map #(apply validate-instructions %))
       (#(if (empty? (filter (fn [item] (= 1 (count item))) %))
          (->> (map ->items %)
               ((fn [x] (html/html-post "/inventories" x)))
               println)
          (io/write-lines file (map first %))))))



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



