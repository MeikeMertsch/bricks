(ns bricks.tmp)


(defn find-in [inventory item]
  (filter #(and (= (:color_id item) (:color_id %))
                (= (:no (:item item)) (:no (:item %)))
                (= (:type (:item item)) (:type (:item %))))
          inventory))
