(ns bricks.color
  (:require [bricks.constants :as const]
            [cheshire.core :as json]
            [com.rpl.specter :as specter]))

(def colors (->> (slurp "resources/colors")
                 (#(json/parse-string % const/transform-to-keywords))))

(defn name->id [name]
  (let [colors-->id [specter/ALL #(= (clojure.string/lower-case name) (:color_name %)) :color_id]
        id (peek (specter/select colors-->id colors))]
    (if id id (throw (Exception. (format "The color %s isn't recognized" name))))))

(defn id->name [color-id]
  (let [colors-->name [specter/ALL #(= color-id (:color_id %)) :color_name]
        name (peek (specter/select colors-->name colors))]
    (if name name (throw (Exception. (format "The color %s isn't recognized" color-id))))))

(defn replace-all-patterns [coll string]
  (loop [replacements coll
         s string]
    (if (empty? replacements)
      s
      (recur (rest replacements)
             (let [[part abb] (first replacements)]
               (clojure.string/replace s (re-pattern part) abb))))))

(defn id->short-name [color-id]
  (->> (id->name color-id)
       (replace-all-patterns [["bright" "B"]
                              ["light" "L"]
                              ["reddish" "R"]
                              ["dark" "D"]
                              ["medium" "M"]
                              ["trans" "T"]
                              ["flat" "F"]
                              ["pearl" "P"]])))
