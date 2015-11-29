(ns bricks.core-test
  (:require [expectations :refer :all]
            [bricks.core :as core]
            [oauth.client :as oauth]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [bricks.constants :as const]))

(def oauth-secrets (json/parse-string (slurp "secrets") const/transform-to-keywords))

(def consumer (oauth/make-consumer (:key oauth-secrets)
                                   (:secret oauth-secrets)
                                   nil
                                   nil
                                   nil
                                   :hmac-sha1))

(defn ->header [site]
  (let [creds (oauth/credentials consumer
                                 (:token oauth-secrets)
                                 (:token-secret oauth-secrets)
                                 :GET
                                 (str const/base-url site))]
    {"Authorization" (str "OAuth realm=\"\",oauth_version=\"1.0\",oauth_consumer_key=\"" (:oauth_consumer_key creds)
                          "\",oauth_token=\"" (:oauth_token creds)
                          "\",oauth_timestamp=\"" (:oauth_timestamp creds)
                          "\",oauth_nonce=\"" (:oauth_nonce creds)
                          "\",oauth_signature_method=\"HMAC-SHA1\",oauth_signature=\"" (:oauth_signature creds)
                          "\"")}))

(defn test-get [site]
  (core/html-get site {:headers (->header site)}))

(defn ->data [html-answer]
  (->> html-answer
       :body
       (#(json/parse-string % const/transform-to-keywords))
       :data))

(defn retry [f max]
  (loop [i 0
         result nil]
    (if (or (>= i max)
            (not= nil result))
      result
      (recur (inc i)
             (f)))))


(println (retry #(->data (test-get "/colors")) 10))


(defn test-get-params []
  (let [params {:email "blah@blah.com"}
        credentials (oauth/credentials consumer
                                       nil
                                       nil
                                       :GET
                                       "https://api.context.io/lite/users"
                                       params)]
    (http/get "https://api.context.io/lite/users" {:query-params (merge credentials params)})))