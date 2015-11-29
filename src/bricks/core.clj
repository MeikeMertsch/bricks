(ns bricks.core
  (:require [cheshire.core :as json]
            [bricks.constants :as const]
            [com.rpl.specter :as specter]))

(def colors (->> (slurp "resources/colors") (#(json/parse-string % const/transform-to-keywords))))

(defn color-id [name]
  (peek (specter/select [specter/ALL #(= name (:color_name %)) :color_id] colors)))

