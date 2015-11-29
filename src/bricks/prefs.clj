(ns bricks.prefs
  (:require [cheshire.core :as json]
            [bricks.html :as html]))

(defn reload-colors []
  (->> (html/html-get "/colors")
       (filter #(not= "Modulex" (:color_type %)))
       (map #(dissoc % :color_code))
       (map #(update-in % [:color_name] clojure.string/lower-case))
       json/generate-string
       (spit "colors")))
