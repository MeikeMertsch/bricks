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

; Validate Instructions
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (validate-instructions "3069b;9;Light Bluish gray" "3069b" 9 86))
(expect ["9;3069b;light bluish gray --> skipped: Some Error"] (validate-instructions "9;3069b;light bluish gray --> skipped: Some Error"))
(expect ["3069b;9;glow in dark white --> skipped: color is not known for that part"] (validate-instructions "3069b;9;glow in dark white" "3069b" 9 159))

; Multiply Inventory with Count of Sets
(let [set (slurp-res "set-inventory")
      set-times-5 (slurp-res "set-inventory-times-5")]
  (expect set-times-5 (multiply-set set 5)))

; Parse Instructions for Deletion
(expect ["3701;black" "3701" 11] (parse-deletions "3701;black"))
(expect ["frnd088;minifig" "frnd088" 0] (parse-deletions "frnd088;minifig"))