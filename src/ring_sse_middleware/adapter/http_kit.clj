;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ring-sse-middleware.adapter.http-kit
  (:require
    [ring-sse-middleware.core :as c]
    [org.httpkit.server       :as hks]))


(defn generate-stream
  "Given a Ring request map, a body generator `(fn []) -> string-body` and options, emit a response stream by
  repeatedly calling the body generator. Options are as follows:
  :cleanup - (fn []) - to cleanup after streaming is over
  :headers - a map   - Ring response map with headers only"
  [request body-generator {:keys [cleanup headers]
                           :or {cleanup (fn [])
                                headers c/sse-empty-response}}]
  (let [running? (atom true)]
    (hks/with-channel request channel
      (hks/on-close channel (fn [status]
                              (reset! running? false)
                              (cleanup)))
      (hks/send! channel headers false)
      (future
        (while @running?
          (let [body (try
                       (body-generator)
                       (catch Throwable e
                         (reset! running? false)
                         (hks/send! channel (format "Error - (%s) %s"
                                              (str (class e)) (.getMessage ^Throwable e)) false)
                         (hks/close channel)
                         (throw e)))]
            (hks/send! channel body false)))))))
