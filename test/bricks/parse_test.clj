(ns bricks.parse-test
  (:require [bricks.parse :refer :all]
            [expectations :refer :all]
            [bricks.test-tools :refer :all]))


(let [set [{:item {:no "3701" :type "PART"} :color_id 11 :quantity 1}
           {:item {:no "3069b" :type "PART"} :color_id 86 :quantity 2}
           {:item {:no "3701" :type "PART"} :color_id 11 :quantity 3}
           {:item {:no "frnd088" :type "PART"} :color_id 0 :quantity 4}
           {:item {:no "3002" :type "PART"} :color_id 1 :quantity 5}]]

  ; Parse Instructions for Update
  (expect ["3701;2;black" "3701" 2 11] (parse-updates-in set "3701;2;black"))
  (expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (parse-updates-in set "3069b;9;Light Bluish gray"))
  (expect #(.startsWith (peek %) "9;3069b;light bluish gray --> skipped: ") (parse-updates-in set "9;3069b;light bluish gray"))
  (expect ["15209;30;white --> skipped: java.lang.Exception: Lot not in set!"] (parse-updates-in set "15209;30;white"))

  ; Parse Instructions for Addition
  (expect ["15209;30;white" "15209" 30 1] (parse-additions-in set "15209;30;white"))
  (expect #(.startsWith (peek %) "9;3069b;light bluish gray --> skipped: ") (parse-additions-in set "9;3069b;light bluish gray"))
  (expect ["3069b;9;Light Bluish gray --> skipped: java.lang.Exception: Lot already in set!"] (parse-additions-in set "3069b;9;Light Bluish gray"))

  ; Parse Instructions for Deletion
  (expect ["3701;black" "3701" 11] (parse-deletions-in set "3701;black"))
  (expect ["frnd088;minifig" "frnd088" 0] (parse-deletions-in set "frnd088;minifig"))
  (expect ["15209;white --> skipped: java.lang.Exception: Lot not in set!"] (parse-deletions-in set "15209;white")))




