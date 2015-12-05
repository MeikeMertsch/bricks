(ns bricks.conversion)

(defn ->int [string]
  (int (bigint string)))

(defn ->item [[_ part quantity color-id]]
  {:item     {:no part :type "PART"}
   :color_id color-id
   :quantity quantity})

(defn ->upload-instruction [unit-price
                            {quantity              :quantity
                             color-id              :color_id
                             {part :no type :type} :item}]
  {:item           {:no   part
                    :type type}
   :color_id       color-id
   :quantity       quantity
   :unit_price     unit-price
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
   :tier_price3    0})

