(ns bricks.html
  (:gen-class)
  (:require [bricks.auth :as auth]
            [clj-http.client :as client]
            [bricks.constants :as const]
            [cheshire.core :as json]))

(defn ->data [html-answer]
  (->> html-answer
       :body
       (#(json/parse-string % const/transform-to-keywords))
       :data))

(defn html-get
  ([url] (html-get url nil))
  ([url params] (auth/retry #(->> (client/get (str const/base-url url) (merge {:headers (auth/->header url)}) params)
                                  ->data) 10)))

(defn html-post [url params]
  (client/post (str const/base-url url) params))





(println (html-get "/inventories"))