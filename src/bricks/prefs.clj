(ns bricks.prefs
  (:require [cheshire.core :as json]
            [bricks.html :as html]))

(defn reload-colors []
  (->> (html/html-get "/colors")
       (filter #(not= "Modulex" (:color_type %)))
       (map #(dissoc % :color_code))
       (map #(update-in % [:color_name] clojure.string/lower-case))
       json/generate-string
       (spit "resources/colors")))

(defn reload-price-guide []
  (->> (html/html-get "/items/part/3795/price"
                      {:type       "part"
                       :no         "3795"
                       :color_id   86
                       :guide_type "sold"})
       json/generate-string
       (spit "resources/price_guide")))