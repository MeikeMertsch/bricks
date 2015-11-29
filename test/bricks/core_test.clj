(ns bricks.core-test
  (:require [bricks.core :refer :all]
            [bricks.html :refer :all]
            [expectations :refer :all]))

(expect 86 (color-id "light bluish gray"))

(expect :avg_price (in (keys (dissoc (html-get "/items/part/3795/price"
                                               {:type       "part"
                                                :no         "3795"
                                                :color_id   86
                                                :guide_type "sold"})
                                     :price_detail))))