(defproject ring-sse-middleware "0.1.3-SNAPSHOT"
  :description "Ring middleware to emit Server-sent Events"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :global-vars {*assert* true
                *warn-on-reflection* true}
  :profiles {:dev {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                  [cheshire "5.8.0"]]}
             :provided {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :c07 {:dependencies [[org.clojure/clojure "1.7.0"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             :c08 {:dependencies [[org.clojure/clojure "1.8.0"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             :c09 {:dependencies [[org.clojure/clojure "1.9.0"]]
                   :global-vars  {*unchecked-math* :warn-on-boxed}}
             :c10 {:dependencies [[org.clojure/clojure "1.10.2"]]
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
  :aliases {"aleph"    ["with-profile" "aleph,dev,c07"]
            "http-kit" ["with-profile" "http-kit,dev,c07"]
            "immutant" ["with-profile" "immutant,dev,c07"]
            "jetty"    ["with-profile" "jetty,dev,c07"]})
