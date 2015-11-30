(ns bricks.core-test
  (:require [bricks.core :refer :all]
            [bricks.html :refer :all]
            [expectations :refer :all]))

(expect 86 (color-id "light bluish gray"))


;;Should test for structure

(let [part-no "3795"
      color "light bluish gray"
      amount 1]
  (expect some? (html-post "/inventories"
                         {:item           {:no   part-no
                                           :type "part"}
                          :color_id       (color-id color)
                          :quantity       amount
                          :unit_price     (price part-no color)
                          :new_or_used    "N"
                          :description    ""
                          :remarks        ""
                          :bulk           1
                          :is_retain      false
                          :is_stock_room  true
                          :stock_room_id  "B"
                          :my_cost        0.0
                          :sale_rate      0
                          :tier_quantity1 0
                          :tier_price1    0
                          :tier_quantity2 0
                          :tier_price2    0
                          :tier_quantity3 0
                          :tier_price3    0})))