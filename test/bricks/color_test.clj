(ns bricks.color-test
  (:require [bricks.color :refer :all]
            [expectations :refer :all]))

; Getting A Color-id By Name
(expect 86 (name->id "light bluish gray"))
(expect Exception (name->id "lbg"))

; Color Validation
(expect true (known-color? "3002" 11))
(expect false (known-color? "3069b" 159))