(ns bricks.sets-test
  (:require [bricks.sets :refer :all]
            [expectations :refer :all]
            [bricks.test-tools :refer :all]))

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

; Add something to a set
(let [set [(second (slurp-res "set-inventory"))]
      new-items [{:item {:no "3701" :type "PART"} :color_id 11 :quantity 2}]
      instructions [["3701;2;black" "3701" 2 11]]]
  (expect (concat set new-items) (add-in-set set instructions)))