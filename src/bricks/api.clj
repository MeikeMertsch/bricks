(ns bricks.api
  (:require [bricks.html :as html]
            [bricks.conversion :as conv]))

(defn download-inventories []
  (html/html-get "/inventories"))

(defn inv-map []
  (let [inv (download-inventories)]
    (-> (map conv/->item-key inv)
        (zipmap inv))))

(defn map-out [set-no]
  (let [inv (inv-map)]
    (->> (html/html-get (format "/items/set/%s/subsets" set-no)
                        {:type          "set"
                         :no            set-no
                         :instruction   true
                         :break_subsets true})
         (map #(get-in % [:entries 0]))
         (map (fn [item] (assoc item :in-stock (some? (inv (conv/->item-key item)))))))))

(defn map-out-minifig [minifig-no]
  (let [inv (inv-map)]
    (->> (html/html-get (format "/items/minifig/%s/subsets" minifig-no)
                        {:type          "MINIFIG"
                         :no            minifig-no
                         :instruction   true
                         :break_subsets true})
         (map #(get-in % [:entries 0]))
         (map (fn [item] (assoc item :in-stock (inv (conv/->item-key item))))))))

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

(defn update-inventories [items-to-update]
  (for [item items-to-update]
    (html/html-put (str "/inventories/" (:inventory_id item))
                   {:quantity (str "+" (:quantity item)) :unit_price (:unit_price item)})))


(defn add-inventories [items-to-add]
  (html/html-post "/inventories" items-to-add))

(defn known-color? [part color-id]
  (->> (html/html-get (str "/items/part/" part "/colors"))
       (filter #(= color-id (:color_id %)))
       ((complement empty?))))