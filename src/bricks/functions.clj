(ns bricks.functions
  (:require [bricks.api :as api]
            [bricks.io :as io]
            [bricks.parse :as parse]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]
            [bricks.color :as color]
            [bricks.constants :as const]))

(defn prepare-print [{color-id :color_id {part :no type-no :type} :item qty :quantity in-stock :in-stock}]
  (let [stock (if in-stock "X" " ")
        formatted-string (format "%s %2s %15s %s" stock qty part (color/id->short-name color-id))]
    (if (= type-no "MINIFIG")
      (->> (api/map-out-minifig part)
           (map prepare-print)
           (cons formatted-string)
           (interpose "\n     ")
           (apply str))
      formatted-string)))


(defn create-checklist [set-no]
  (->> (api/map-out set-no)
       (sort-by (comp color/id->name :color_id))
       (map prepare-print)
       (io/write-lines (format "checklists/%s" set-no))))

;(create-checklist "60099-1")
;(create-checklist "75097-1")

(defn create-labels [set-no]
  (->> (api/map-out set-no)
       (sort-by (comp color/id->name :color_id))
       (map (fn [{color-id :color_id {part :no type-no :type} :quantity in-stock :in-stock}]
              (let [formatted-string (format "%s %s %s" (if in-stock "X" " ") part (color/id->short-name color-id))]
                (if (= type-no "MINIFIG")
                  (->> (api/map-out-minifig part)
                       (map prepare-print)
                       (cons formatted-string)
                       (interpose "\n")
                       (apply str))
                  formatted-string))))
       (io/write-lines (format "labels/%s" set-no))))

(def new_sets ["41040-1"
               "41545-1"
               "41547-1"
               "41551-1"
               "41553-1"
               "41548-1"
               "79016-1"])

;(println (map create-checklist new_sets))
#_(clojure.pprint/pprint (api/map-out "41545-1"))


(defn set-subset-labels-for-printing [set-base-no]
  (->> (mapcat (fn [set-no] (->> (api/map-out set-no)
                                 (sort-by (comp color/id->name :color_id))
                                 (map prepare-print)
                                 (cons set-no)))
               (map #(format "%s-%s" set-base-no %) (range 2 (inc 25))))
       (io/write-lines (format "labels/%s" set-base-no))))

;(set-subset-labels-for-printing "41040")
;(set-subset-labels-for-printing "60099")
;(set-subset-labels-for-printing "75097")


(defn part-out-set [set-no quantity delete-file update-file additions-file margin-set-price]
  (let [parts (sets/multiply-set (api/map-out set-no) quantity)
        deletions (io/read-with-parser delete-file (partial parse/parse-deletions-in parts))
        updates (io/read-with-parser update-file (partial parse/parse-updates-in parts))
        additions (io/read-with-parser additions-file (partial parse/parse-additions-in parts))
        inventory (api/download-inventories)]
    (-> (sets/delete-in-set parts deletions)                ;update to zero?
        (sets/update-in-set updates)                        ; needs error handling
        (sets/add-in-set additions)
        (#(map (partial conv/->upload-instruction
                        (sets/lot-price % margin-set-price quantity))
               %))
        (sets/group-duplicates inventory)
        (#(let [items-to-add (map first (filter (fn [item] (= 1 (count item))) %))
                items-to-update (filter (fn [item] (not= 1 (count item))) %)]
           (api/add-inventories items-to-add)
           (api/push-update items-to-update))))))

;(part-out-set "Swmagpromo-1" 93 const/empty-file const/empty-file const/empty-file 15.625)
;(part-out-set "30256-1" 21 "resources/30256-1-deletions" "resources/30256-1-updates" const/empty-file 34.375)
;(part-out-set "5994-1" 75 const/empty-file "resources/5994-1-updates" const/empty-file 26.3)
;(part-out-set "75104-1" 1 "resources/75104-1-deletions" "resources/75104-1-updates" "resources/75104-1-additions" 1171.875)
;(part-out-set "41044-1" 10 "resources/41044-1-deletions" "resources/41044-1-updates" "resources/41044-1-additions" 31.25)
;(part-out-set "41040-1" 3 "resources/41040-1-deletions" "resources/41040-1-updates" "resources/41040-1-additions" 236.25)
;(part-out-set "41102-1" 7 "resources/41102-1-deletions" "resources/41102-1-updates" const/empty-file 1.00)
