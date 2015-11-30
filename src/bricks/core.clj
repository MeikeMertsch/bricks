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