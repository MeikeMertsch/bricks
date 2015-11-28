(ns bricks.core-test
  (:require [expectations :refer :all]
            [bricks.core :as core]))


(defn headers []
 {"Authorization" (slurp "secret")})

(println (core/html-get "/inventories" {:headers (headers)}))


