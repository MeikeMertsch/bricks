(ns bricks.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [bricks.constants :as const]))


(defn html-get
  ([url] (html-get url nil))
  ([url params] (client/get (str const/base-url url) params)))

(defn html-post [url params]
  (client/post (str const/base-url url) params))