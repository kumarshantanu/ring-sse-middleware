;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ring-sse-middleware.adapter.generic
  (:require
    [clojure.java.io    :as java-io]
    [ring.util.io       :as ring-io]
    [ring.util.response :as response]
    [ring-sse-middleware.core :as c])
  (:import
    [java.io IOException OutputStream Writer]))


(defn generate-stream
  "Given a Ring request map, a body generator `(fn []) -> string-body` and options, emit a response stream by
  repeatedly calling the body generator. Options are as follows:
  :cleanup - (fn []) - to cleanup after streaming is over
  :headers - a map   - Ring response map with headers only"
  [request body-generator {:keys [cleanup headers]
                           :or {cleanup (fn [])
                                headers c/sse-empty-response}
                           :as options}]
  (assoc headers
    :body (ring-io/piped-input-stream
            (fn [^OutputStream out]
              (try
                (let [^Writer writer (java-io/make-writer out {})]
                  (while true
                    (doto writer
                      (.write ^String (body-generator))
                      (.flush))))
                (catch IOException e
                  (when-not (= "Pipe closed" (.getMessage e))
                    (throw e)))
                (finally
                  (cleanup)))))))
