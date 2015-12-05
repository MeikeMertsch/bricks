(ns bricks.conversion-test
  (:require [bricks.conversion :refer :all]
            [bricks.test-tools :refer :all]
            [expectations :refer :all]))

(expect "0.55" (divide 0.545M 1))
(expect "0.54" (divide 0.544M 1))
(expect "1.50" (divide 3 2M))
(expect "2.00" (divide 8 4M))


; Turn set into Upload Instructions
(let [set (slurp-res "set-inventory")]
  (expect {:remarks        "",
           :description    "",
           :is_stock_room  true,
           :tier_price3    0,
           :sale_rate      0,
           :unit_price     "0.05",
           :tier_price2    0,
           :bulk           1,
           :item           {:no "3005", :type "PART"},
           :tier_quantity2 0,
           :stock_room_id  "B",
           :tier_price1    0,
           :tier_quantity1 0,
           :tier_quantity3 0,
           :my_cost        0.0,
           :color_id       1,
           :new_or_used    "N", :quantity 2, :is_retain false}
          (->upload-instruction 0.10 (first set))))