(defproject bricks "0.1.0-SNAPSHOT"
  :description "This project helps me learning more Clojure and is intended to help uploading parts easier to Bricklink by communicating with its API and to adjust pricing."
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [clj-http "2.0.0"]
                 [clj-oauth "1.5.3"]
                 [clj-time "0.11.0"]
                 [com.cemerick/url "0.1.1"]
                 [com.cemerick/url "0.1.1"]
                 [com.rpl/specter "0.7.1"]
                 [expectations "2.1.2"]]
  :main ^:skip-aot bricks.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
