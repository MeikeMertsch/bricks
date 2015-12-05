(ns bricks.functions-test
  (:require [bricks.functions :refer :all]
            [expectations :refer :all]))

(expect "0.55" (divide 0.545M 1))
(expect "0.54" (divide 0.544M 1))
(expect "1.50" (divide 3 2M))
(expect "2.00" (divide 8 4M))