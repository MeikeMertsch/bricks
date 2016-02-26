(ns bricks.parse
  (:require [bricks.color :as color]
            [bricks.conversion :as conv]))

(defn parse-confirmed [line]
  (let [[_ qty part color-name type] (take 5 (clojure.string/split line #";"))]
    (conv/->item [_ part (int (bigint qty)) (color/short-name->id color-name)] type)))