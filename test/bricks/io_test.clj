(ns bricks.io-test
  (:require [bricks.io :refer :all]
            [expectations :refer :all]
            [bricks.test-tools :refer :all]))


; Parse Instructions for upload
(expect ["3701;2;black" "3701" 2 11] (parse-updates "3701;2;black"))
(expect ["3069b;9;Light Bluish gray" "3069b" 9 86] (parse-updates "3069b;9;Light Bluish gray"))
(expect #(.startsWith (peek %) "9;3069b;light bluish gray --> skipped: ") (parse-updates "9;3069b;light bluish gray"))

; Parse Instructions for Deletion
(expect ["3701;black" "3701" 11] (parse-deletions "3701;black"))
(expect ["frnd088;minifig" "frnd088" 0] (parse-deletions "frnd088;minifig"))




