(ns bricks.functions
  (:require [bricks.html :as html]
            [bricks.io :as io]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]))


(defn upload-inventories [file]
  (->> io/parse-updates
       (io/parse-lines-with-f file)
       (map #(apply sets/validate-instructions %))
       (#(if (empty? (filter (fn [item] (= 1 (count item))) %))
          (->> (map conv/->items %)
               ((fn [x] (html/html-post "/inventories" x)))
               println)
          (io/write-lines file (map first %))))))

(defn part-out-set [set-no quantity delete-file update-file additions-file margin-set-price]
  (let [inventory (sets/multiply-set (sets/part-out set-no) quantity)
        deletions (io/parse-lines-with-f delete-file io/parse-deletions)
        updates (io/parse-lines-with-f update-file io/parse-updates)
        additions (io/parse-lines-with-f additions-file io/parse-updates)]
    (-> (sets/delete-in-set inventory deletions)
        (sets/update-in-set updates)
        (sets/add-in-set additions)
        (#(let [sum-parts (sets/count-parts %)
                price (* margin-set-price quantity)
                unit-price (/ price sum-parts)]
           (->> (map (partial conv/->upload-instruction unit-price) %)
           (html/html-post "/inventories")))))))


;(println (part-out-set "41040-21" 2 "resources/file-deletions" "resources/file-updates" "resources/file-additions" 20))