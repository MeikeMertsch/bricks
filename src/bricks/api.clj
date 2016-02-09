(ns bricks.api
  (:require [bricks.html :as html]
            [bricks.conversion :as conv]))

(defn download-inventories []
  (html/html-get "/inventories"))

(defn triple-out [{col :color_id {no :no type :type} :item}]
  [col no type])

(defn inv-map []
  (let [inv (download-inventories)]
    (-> (map triple-out inv)
        (zipmap inv))))


(defn map-out [set-no]
  (let [inv (inv-map)]
    (->> (html/html-get (format "/items/set/%s/subsets" set-no)
                        {:type          "set"
                         :no            set-no
                         :instruction   true
                         :break_subsets true})
         (map #(get-in % [:entries 0]))
         (map (fn [item] (assoc item :in-stock (inv (triple-out item))))))))

(defn map-out-minifig [minifig-no]
  (let [inv (inv-map)]
    (->> (html/html-get (format "/items/minifig/%s/subsets" minifig-no)
                        {:type          "MINIFIG"
                         :no            minifig-no
                         :instruction   true
                         :break_subsets true})
         (map #(get-in % [:entries 0]))
         (map (fn [item] (assoc item :in-stock (inv (triple-out item))))))))

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

(defn add-inventories [items-to-add]
  (html/html-post "/inventories" items-to-add))

(defn known-color? [part color-id]
  (->> (html/html-get (str "/items/part/" part "/colors"))
       (filter #(= color-id (:color_id %)))
       ((complement empty?))))