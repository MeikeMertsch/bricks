(ns bricks.functions
  (:require [bricks.html :as html]
            [bricks.io :as io]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]
            [cheshire.core :as json]))

(defn download-inventories []
  (html/html-get "/inventories"))



(defn upload-inventories [file]
  (->> io/parse-updates
       (io/parse-lines-with-f file)
       (map #(apply sets/validate-instructions %))
       (#(if (empty? (filter (fn [item] (= 1 (count item))) %))
          (->> (map conv/->items %)
               ((fn [x] (html/html-post "/inventories" x)))
               println)
          (io/write-lines file (map first %))))))

(defn unit-price [set margin-set-price quantity]
  (let [sum-parts (sets/count-parts set)
        price (* margin-set-price quantity)]
    unit-price (/ price sum-parts)))

(defn part-out-set [set-no quantity delete-file update-file additions-file margin-set-price]
  (let [set (sets/multiply-set (sets/part-out set-no) quantity)
        deletions (io/parse-lines-with-f delete-file io/parse-deletions)
        updates (io/parse-lines-with-f update-file io/parse-updates)
        additions (io/parse-lines-with-f additions-file io/parse-updates)
        inventory (download-inventories)]
    (-> (sets/delete-in-set set deletions) ; needs error handling
        (sets/update-in-set updates) ; needs error handling
        (sets/add-in-set additions)
        ;; compare with current online inventory
        ;; sort out additions vs. updates of existing inventory
        (#(->> (map (partial conv/->upload-instruction
                             (unit-price % margin-set-price quantity)) %)
               (html/html-post "/inventories"))))))


;(println (part-out-set "41040-21" 2 "resources/file-deletions" "resources/file-updates" "resources/file-additions" 20))

;(println (part-out-set "Swmagpromo-1" 93 const/empty-file const/empty-file const/empty-file 12.5))
;(println (part-out-set "30256-1" 21 "resources/30256-1-deletions" "resources/30256-1-updates" const/empty-file 27.5))
;(println (part-out-set "5994-1" 75 const/empty-file "resources/5994-1-updates" const/empty-file 21.04))
;(println (part-out-set "41044-1" 10 "resources/41044-1-deletions" "resources/41044-1-updates" "resources/41044-1-additions" ))
;(println (part-out-set "41040-1" 3 "resources/41040-1-deletions" "resources/41040-1-updates" const/empty-file ))
;(println (part-out-set "75104-1" 1 "resources/75104-1-deletions" "resources/75104-1-updates" "resources/75104-1-additions" 937.5))
