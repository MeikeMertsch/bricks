(ns bricks.core-test
  (:require [bricks.core :refer :all]
            [bricks.html :refer :all]
            [expectations :refer :all]))

(expect 86 (color-id "light bluish gray"))
(expect Exception (color-id "lbg"))


; Parse Instructions for upload
(expect ["3701;2;black" "3701" 2 11] (parse-upload-instructions "3701;2;black"))
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (parse-upload-instructions "3069b;9;Light Bluish gray"))
(expect #(.startsWith (peek %) "9;3069b;light bluish gray --> skipped: ") (parse-upload-instructions "9;3069b;light bluish gray"))

; Validate Instructions
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (validate-instructions "3069b;9;Light Bluish gray" "3069b" 9 86))
(expect ["9;3069b;light bluish gray --> skipped: Some Error"] (validate-instructions "9;3069b;light bluish gray --> skipped: Some Error"))
(expect #(.startsWith (peek %) "3069b;9;glow in dark white --> skipped: color is not known for that part") (validate-instructions "3069b;9;glow in dark white" "3069b" 9 159))



(expect true (known-color? "3002" 11))
(expect false (known-color? "3069b" 159))


        ;; Should test for structure

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