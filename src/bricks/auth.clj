(ns bricks.auth
  (:require [bricks.constants :as const]
            [oauth.client :as oauth]
            [cheshire.core :as json]))

(def oauth-secrets (json/parse-string (slurp "secrets") const/transform-to-keywords))

(defn retry [f pred init max]
  (loop [i 0
         result init]
    (if (or (>= i max)
            (pred result))
      result
      (recur (inc i)
             (f)))))

(def consumer (oauth/make-consumer (:key oauth-secrets)
                                   (:secret oauth-secrets)
                                   nil
                                   nil
                                   nil
                                   :hmac-sha1))

(defn ->header [site params method]
  (let [creds (oauth/credentials consumer
                                 (:token oauth-secrets)
                                 (:token-secret oauth-secrets)
                                 method
                                 (str const/base-url site)
                                 params)]
    {"Authorization" (str "OAuth realm=\"\",oauth_version=\"1.0\",oauth_consumer_key=\"" (:oauth_consumer_key creds)
                          "\",oauth_token=\"" (:oauth_token creds)
                          "\",oauth_timestamp=\"" (:oauth_timestamp creds)
                          "\",oauth_nonce=\"" (:oauth_nonce creds)
                          "\",oauth_signature_method=\"HMAC-SHA1\",oauth_signature=\"" (:oauth_signature creds)
                          "\"")}))