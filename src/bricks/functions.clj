(ns bricks.functions
  (:require [bricks.api :as api]
            [bricks.io :as io]
            [bricks.parse :as parse]
            [bricks.sets :as sets]
            [bricks.conversion :as conv]
            [bricks.color :as color]
            [clj-time.core :as t]
            [clj-time.format :as f]))

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


(defn create-labels [set-no]
  (->> (api/map-out set-no)
       (sort-by (juxt #(color/id->name (:color_id %)) #(:name (:item %))))
       (map (fn [{color-id :color_id {part :no type-no :type} :item in-stock :in-stock}]
              (let [formatted-string (format "%s %s %s" (if in-stock "X" " ") part (color/id->short-name color-id))]
                (if (= type-no "MINIFIG")
                  (->> (api/map-out-minifig part)
                       (map prepare-print)
                       (cons formatted-string)
                       (interpose "\n")
                       (apply str))
                  formatted-string))))
       (io/write-lines (format "labels/%s" set-no))))


;(println (create-labels "41044-1"))


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
;(println (create-checklist "41040-1" 3))
;(println (create-checklist "41102-1" 7))
;(println (create-checklist "75097-1" 15))
;(println (create-checklist "41545-1" 1))
;(println (create-checklist "41547-1" 1))
;(println (create-checklist "41551-1" 1))
;(println (create-checklist "41553-1" 1))
;(println (create-checklist "41548-1" 1))
;(println (create-checklist "79016-1" 1))
;(println (create-checklist "7641-1" 1))



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
     :adding #_(api/add-inventories additions)
     :updating #_(api/update-inventories updates)]))

;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-12-Swmagpromo-1" 15.625 93))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-12-30256-1" 34.375 21))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-5994-1" 26.3 75))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-75104-1" 1171.875 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41044-1" 31.25 10))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41040-1" 236.25 3))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41102-1" 249.0 7))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-75097-1" 279.0 15))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41545-1" 27.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41547-1" 27.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-13-41551-1" 27.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-14-41553-1" 27.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-14-41548-1" 27.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-14-79016-1" 358.0 1))
;;;(clojure.pprint/pprint (read-confirmed-set "2016-02-18-7641-1" 650.0 1))
