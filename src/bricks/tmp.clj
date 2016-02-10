(ns bricks.tmp
  (:require [bricks.conversion :as conv]))


(defn find-in [inventory item]
  (filter #(and (= (:color_id item) (:color_id %))
                (= (:no (:item item)) (:no (:item %)))
                (= (:type (:item item)) (:type (:item %))))
          inventory))

(defn find-item [inv-map item]
  (inv-map (conv/->item-key item)))