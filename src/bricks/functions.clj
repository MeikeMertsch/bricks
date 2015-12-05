(ns bricks.functions
  (:require [bricks.html :as html]
            [bricks.io :as io]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]))

(defn download-inventories []
  (html/html-get "/inventories"))

(defn divide [divisor dividend]
  (->> (/ divisor dividend)
       (with-precision 10)
       (format "%.2f")))


(defn upload-inventories [file]
  (->> io/parse-updates
       (io/parse-lines-with-f file)
       (map #(apply sets/validate-instructions %))
       (#(if (empty? (filter (fn [item] (= 1 (count item))) %))
          (->> (map conv/->items %)
               ((fn [x] (html/html-post "/inventories" x)))
               println)
          (io/write-lines file (map first %))))))

(defn pcs-price [set margin-set-price quantity]
  (let [sum-parts (sets/count-parts set)
        price (* margin-set-price quantity)]
    (println (format "%s parts in %s sets with price: %s" sum-parts quantity margin-set-price))
    (divide price sum-parts)))

(defn push-update [items-to-update]
  (for [item items-to-update
        :let [new (first item)
              old (last item)
              total-pcs (+ (:quantity old) (:quantity new))
              price-sum (+ (* (:quantity old) (bigdec (:unit_price old)))
                           (* (:quantity new) (bigdec (:unit_price new))))
              new-price (divide price-sum total-pcs)]]
    (html/html-put (str "/inventories/" (:inventory_id old))
                   {:quantity (str "+" (:quantity new)) :unit_price new-price})))



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
                        (pcs-price % margin-set-price quantity))
               %))
        (sets/check-inventory inventory)
        (#(let [items-to-add (map first (filter (fn [item] (= 1 (count item))) %))
                items-to-update (filter (fn [item] (not= 1 (count item))) %)]
           (html/html-post "/inventories" items-to-add)
           (push-update items-to-update))))))


;(println (part-out-set "41040-21" 2 "resources/file-deletions" "resources/file-updates" "resources/file-additions" 20))

;(part-out-set "Swmagpromo-1" 93 const/empty-file const/empty-file const/empty-file 12.5)
;(part-out-set "30256-1" 21 "resources/30256-1-deletions" "resources/30256-1-updates" const/empty-file 27.5)
;(part-out-set "5994-1" 75 const/empty-file "resources/5994-1-updates" const/empty-file 21.04)
;(part-out-set "75104-1" 1 "resources/75104-1-deletions" "resources/75104-1-updates" "resources/75104-1-additions" 937.5)

;(part-out-set "41044-1" 10 "resources/41044-1-deletions" "resources/41044-1-updates" "resources/41044-1-additions" )
;(part-out-set "41040-1" 3 "resources/41040-1-deletions" "resources/41040-1-updates" const/empty-file )
