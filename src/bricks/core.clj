(ns bricks.core
  (:require [cheshire.core :as json]
            [bricks.constants :as const]
            [com.rpl.specter :as specter]
            [bricks.html :as html]))

(def colors (->> (slurp "resources/colors") (#(json/parse-string % const/transform-to-keywords))))

(defn color-id [name]
  (let [id
  (peek (specter/select [specter/ALL #(= name (:color_name %)) :color_id] colors))]
    (if id id (throw (Exception. "The color isn't recognized")))))

(defn price [number color]
  (->> (html/html-get (str "/items/part/" number "/price")
                      {:type       "part"
                       :no         number
                       :color_id   (color-id color)
                       :guide_type "sold"})
       :avg_price))

; TODO:
(defn upload-inventories [file]
  ; Load file
  ; Parse Instructions
  ; Validate Instructions
  ;; ON PASS
  ;;; Instructions -> Items
  ;;; POST
  ;; ON FAIL
  ;;; Spit Intructions + Error
  )

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



