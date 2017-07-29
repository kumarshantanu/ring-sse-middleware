(defproject ring-sse-middleware "0.1.1"
  :description "Ring middleware to emit Server-sent Events"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :global-vars {*assert* true
                *warn-on-reflection* true}
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                  [cheshire "5.7.1"]]}
             :c16 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :c17 {:dependencies [[org.clojure/clojure "1.7.0"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             :c18 {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             :c19 {:dependencies [[org.clojure/clojure "1.9.0-alpha17"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             ;; test profiles
             :aleph    {:dependencies [[aleph "0.4.3"]]
                        :jvm-opts ["-Dmanifold.disable-jvm8-primitives=true"]
                        :main ring-sse-middleware.aleph-test}
             :immutant {:dependencies [[org.immutant/immutant "2.1.9"]]
                        :main ring-sse-middleware.immutant-test}
             :jetty    {:dependencies [[ring/ring-core "1.4.0"]
                                       [ring/ring-jetty-adapter "1.4.0"]]
                        :main ring-sse-middleware.jetty-test}
             :http-kit {:dependencies [[http-kit "2.2.0"]]
                        :main ring-sse-middleware.http-kit-test}
             :perf {:dependencies [[citius "0.2.4"]]
                    :jvm-opts ^:replace ["-server" "-Xms2048m" "-Xmx2048m"]}}
  :aliases {"aleph"    ["with-profile" "aleph,dev,c16"]
            "http-kit" ["with-profile" "http-kit,dev,c16"]
            "immutant" ["with-profile" "immutant,dev,c16"]
            "jetty"    ["with-profile" "jetty,dev,c16"]})
