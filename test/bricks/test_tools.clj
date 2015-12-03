(ns bricks.test-tools
  (:require [cheshire.core :as json]
            [bricks.constants :as const]))

(defn slurp-res [file]
  (json/parse-string (slurp (str "resources/" file)) const/transform-to-keywords))

