(ns bricks.functions-test
  (:require [bricks.functions :refer :all]
            [expectations :refer :all]
            [cheshire.core :as json]
            [bricks.constants :as const]))

(defn slurp-res [file]
  (json/parse-string (slurp (str "resources/" file)) const/transform-to-keywords))


; Parse Instructions for upload
(expect ["3701;2;black" "3701" 2 11] (parse-upload-instructions "3701;2;black"))
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (parse-upload-instructions "3069b;9;Light Bluish gray"))
(expect #(.startsWith (peek %) "9;3069b;light bluish gray --> skipped: ") (parse-upload-instructions "9;3069b;light bluish gray"))

; Parse Instructions for Deletion
(expect ["3701;black" "3701" 11] (parse-deletions "3701;black"))
(expect ["frnd088;minifig" "frnd088" 0] (parse-deletions "frnd088;minifig"))

; Validate Instructions
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (validate-instructions "3069b;9;Light Bluish gray" "3069b" 9 86))
(expect ["9;3069b;light bluish gray --> skipped: Some Error"] (validate-instructions "9;3069b;light bluish gray --> skipped: Some Error"))
(expect ["3069b;9;glow in dark white --> skipped: color is not known for that part"] (validate-instructions "3069b;9;glow in dark white" "3069b" 9 159))

; Multiply Inventory with Count of Sets
(let [set (slurp-res "set-inventory")
      set-times-5 (slurp-res "set-inventory-times-5")]
  (expect set-times-5 (multiply-set set 5)))

; Delete from set inventory
(let [set (slurp-res "minifig-set")
      reduced-set (slurp-res "minifig-set-no-minifig")]
  (expect reduced-set (delete-in-set set [["loc127;minifig" "loc127" 0]]))
  (expect set (delete-in-set set [["bcc1;black" "bcc1" 11]])))

; Update in set inventory
(let [set (slurp-res "minifig-set")
      updated-set (slurp-res "minifig-set-five-minifigs")]
  (expect updated-set (update-in-set set [["loc127;5;minifig" "loc127" 5 0]])))

; Calculate Sum of parts
(let [set (slurp-res "set-inventory")]
  (expect 23 (count-parts set)))

(let [set (slurp-res "set-inventory")]
(expect {:remarks "",
         :description "",
         :is_stock_room true,
         :tier_price3 0,
         :sale_rate 0,
         :unit_price 0.1,
         :tier_price2 0,
         :bulk 1,
         :item {:no "3005", :type "PART"},
         :tier_quantity2 0,
         :stock_room_id "B",
         :tier_price1 0,
         :tier_quantity1 0,
         :tier_quantity3 0,
         :my_cost 0.0,
         :color_id 1,
         :new_or_used "N", :quantity 2, :is_retain false}
        (->upload-instruction 0.10 (first set))))
