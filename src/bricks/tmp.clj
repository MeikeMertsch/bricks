(ns bricks.tmp
  (:require [bricks.api :as api]))


(defn find-in [inventory item]
  (filter #(and (= (:color_id item) (:color_id %))
                (= (:no (:item item)) (:no (:item %)))
                (= (:type (:item item)) (:type (:item %))))
          inventory))

(defn find-item [inv-map item]
  (inv-map (api/triple-out item)))