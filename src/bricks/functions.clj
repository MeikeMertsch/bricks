(ns bricks.functions
  (:require [bricks.html :as html]
            [bricks.io :as io]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]
            [bricks.constants :as const]
            [bricks.color :as color]))

(defn download-inventories []
  (html/html-get "/inventories"))

(defn lot-price [set margin-set-price quantity]
  (let [sum-lots (count set)
        price (* margin-set-price quantity)]
    (println (format "%s lots in %s sets with price: %s" sum-lots quantity margin-set-price))
    (conv/divide price sum-lots)))

(defn push-update [items-to-update]
  (for [item items-to-update
        :let [new (first item)
              old (last item)
              total-pcs (+ (:quantity old) (:quantity new))
              price-sum (+ (* (:quantity old) (bigdec (:unit_price old)))
                           (* (:quantity new) (bigdec (:unit_price new))))
              new-price (conv/divide price-sum total-pcs)]]
    (html/html-put (str "/inventories/" (:inventory_id old))
                   {:quantity (str "+" (:quantity new)) :unit_price new-price})))


(defn sort-update-file [file]
  (->> (io/parse-lines-with-f file io/parse-updates)
       (sort-by (juxt second #(color/color-name (last %))))
       (map first)
       (io/write-lines file)))

;(sort-update-file "resources/30256-1-updates")
;(sort-update-file "resources/5994-1-updates")
;(sort-update-file "resources/75104-1-updates")
;(sort-update-file "resources/41044-1-updates")
;(sort-update-file "resources/41040-1-updates")
;(sort-update-file "resources/41102-1-updates")

(defn part-out-set [set-no quantity delete-file update-file additions-file margin-set-price]
  (let [set (sets/multiply-set (sets/part-out set-no) quantity)
        deletions (io/parse-lines-with-f delete-file io/parse-deletions)
        updates (io/parse-lines-with-f update-file io/parse-updates)
        additions (io/parse-lines-with-f additions-file io/parse-updates)
        inventory (download-inventories)]
    (-> (sets/delete-in-set set deletions)                  ; needs error handling
        (sets/update-in-set updates)                        ; needs error handling
        (sets/add-in-set additions)
        (#(map (partial conv/->upload-instruction
                        (lot-price % margin-set-price quantity))
               %))
        (sets/check-inventory inventory)
        (#(let [items-to-add (map first (filter (fn [item] (= 1 (count item))) %))
                items-to-update (filter (fn [item] (not= 1 (count item))) %)]
           (html/html-post "/inventories" items-to-add)
           (push-update items-to-update))))))




;(part-out-set "Swmagpromo-1" 93 const/empty-file const/empty-file const/empty-file 15.625)
;(part-out-set "30256-1" 21 "resources/30256-1-deletions" "resources/30256-1-updates" const/empty-file 34.375)
;(part-out-set "5994-1" 75 const/empty-file "resources/5994-1-updates" const/empty-file 26.3)
;(part-out-set "75104-1" 1 "resources/75104-1-deletions" "resources/75104-1-updates" "resources/75104-1-additions" 1171.875)
;(part-out-set "41044-1" 10 "resources/41044-1-deletions" "resources/41044-1-updates" "resources/41044-1-additions" 31.25)
;(part-out-set "41040-1" 3 "resources/41040-1-deletions" "resources/41040-1-updates" "resources/41040-1-additions" 236.25)
;(part-out-set "41102-1" 7 "resources/41102-1-deletions" "resources/41102-1-updates" const/empty-file )