(ns bricks.core-test
  (:require [bricks.core :refer :all]
            [expectations :refer :all]))

(expect 86 (color-id "light bluish gray"))