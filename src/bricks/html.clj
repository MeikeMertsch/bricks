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
  ([url params] (->> (auth/retry #(client/get (str const/base-url url)
                                              (merge {:headers (auth/->header url params :GET)}
                                                     {:query-params params})) 10)
                     ->data)))

(defn html-post [url params]
  (println (format "POST result: %s params: %s"
                   (auth/->meta (auth/retry #(client/post (str const/base-url url)
                                                          (merge {:headers      (auth/->header url nil :POST)
                                                                  :content-type "application/json"}
                                                                 {:body (json/generate-string params)}))
                                            10))
                   params)))


(defn html-put [url params]
  (println (format "PUT result: %s url: %s params: %s"
                   (auth/->meta (auth/retry #(client/put (str const/base-url url)
                                                         (merge {:headers      (auth/->header url nil :PUT)
                                                                 :content-type "application/json"}
                                                                {:body (json/generate-string params)}))
                                            10))
                   url
                   params)))


(defn temp [path]
  (-> (slurp path)
      (json/parse-string const/transform-to-keywords)))
