(ns bricks.functions
  (:require [bricks.api :as api]
            [bricks.io :as io]
            [bricks.parse :as parse]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]
            [bricks.color :as color]
            [bricks.constants :as const]
            [bricks.html :as html]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [cheshire.core :as json]))

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


(defn ->csv [{{part :no name :name type :type} :item qty :quantity e-qty :extra_quantity :color-id :color_id in-stock :in-stock}]
  (format "%s\t%s\t%s\t%s\t%s\t%s" (if in-stock "x" " ") (- qty e-qty) part (color/id->short-name color-id) type name))

(def done-sets ["Swmagpromo-1"
                "30256-1"
                "5994-1"
                "75104-1"
                "41044-1"
                "41040-1"
                "41102-1"
                "41545-1"
                "41547-1"
                "41551-1"
                "41553-1"
                "41548-1"
                "79016-1"])

#_(println (map #(->> (api/map-out %)
                      json/generate-string
                      (spit (str "parted-out/" %))) done-sets))



(defn create-checklist [input qty]
  (->> (api/map-out input)
    #_(html/temp (str "parted-out/" input))
    (#(sets/multiply-set % qty))
    (sort-by (juxt #(color/id->name (:color_id %)) #(:name (:item %))))
    (map ->csv)
    (io/write-lines (str "checklists/" (f/unparse (f/formatter "yyyy-MM-dd-") (t/now)) input ".csv"))))


;   (println (create-checklist "Swmagpromo-1" 93))
;   (println (create-checklist "30256-1" 21))
;   (println (create-checklist "5994-1" 75))
;(println (create-checklist "75104-1" 1))
;(println (create-checklist "41044-1" 10))
(println (create-checklist "41040-1" 3))

#_(
    (println (create-checklist "41040-1" 3))
    (println (create-checklist "41102-1" 7))
    (println (create-checklist "41545-1" 1))
    (println (create-checklist "41547-1" 1))
    (println (create-checklist "41551-1" 1))
    (println (create-checklist "41553-1" 1))
    (println (create-checklist "41548-1" 1))
    )

(defn read-confirmed-set [file margin-set-price qty]
  (let [items (io/read-with-parser (str "confirmed/" file ".csv") parse/parse-confirmed)
        lot-price (sets/lot-price items margin-set-price qty)
        inv (group-by conv/->item-key (api/download-inventories))
        grouped (group-by #(nil? (inv (conv/->item-key %))) items)
        additions (map #(conv/->upload-instruction lot-price %) (grouped true []))
        updates (map (fn [item]
                       (let [i-item (first (inv (conv/->item-key item)))
                             total-pcs (+ (:quantity i-item) (:quantity item))
                             price-sum (+ (* (:quantity i-item) (bigdec (:unit_price i-item))) (bigdec lot-price))
                             new-price (conv/divide price-sum total-pcs)]
                         {:inventory_id (:inventory_id i-item)
                          :quantity     (:quantity item)
                          :unit_price   new-price})) (grouped false []))]
    [:additions (grouped true)
     :updates updates
     :adding (api/add-inventories additions)
     :updating (api/update-inventories updates)]))

;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-12-Swmagpromo-1" 15.625 93))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-12-30256-1" 34.375 21))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-5994-1" 26.3 75))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-75104-1" 1171.875 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41044-1" 31.25 10))


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
