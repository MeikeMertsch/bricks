(ns bricks.color
  (:require [bricks.constants :as const]
            [bricks.html :as html]
            [cheshire.core :as json]
            [com.rpl.specter :as specter]))

(def colors (->> (slurp "resources/colors")
                 (#(json/parse-string % const/transform-to-keywords))))

(defn color-id [name]
  (let [colors-->id [specter/ALL #(= (clojure.string/lower-case name) (:color_name %)) :color_id]
        id (peek (specter/select colors-->id colors))]
    (if id id (throw (Exception. "The color isn't recognized")))))

(defn color-name [color_id]
  (let [colors-->name [specter/ALL #(= color_id (:color_id %)) :color_name]
        name (peek (specter/select colors-->name colors))]
    (if name name (throw (Exception. (format "The color %s isn't recognized" color_id))))))

(defn known-color? [part color-id]
  (->> (html/html-get (str "/items/part/" part "/colors"))
       (filter #(= color-id (:color_id %)))
       ((complement empty?))))
