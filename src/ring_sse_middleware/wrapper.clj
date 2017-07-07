;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ring-sse-middleware.wrapper)


(defn sleep
  "Sleep for specified number of milliseconds."
  [^long millis]
  (try
    (Thread/sleep millis)
    (catch InterruptedException e
      (.interrupt (Thread/currentThread)))))


(defn wrap-delay
  "Given number of milliseconds to sleep, wrap fn f such that it always sleeps for specified duration before executing.
  This function is assumed to be called by a single thread, so we treat atom like a volatile field."
  [f sleep-millis]
  (let [{:keys [^long initial ^long interval]
         :or {initial 0 interval 0}} (cond
                                       (integer? sleep-millis) {:initial 0
                                                                :interval sleep-millis}
                                       (map? sleep-millis)     sleep-millis
                                       (vector? sleep-millis)  {:initial  (first sleep-millis)
                                                                :interval (second sleep-millis)}
                                       :otherwise (throw (IllegalArgumentException.
                                                           (str "Expected long, map or vector, but found ("
                                                             (class sleep-millis) ") " (pr-str sleep-millis)))))
        started? (atom false)
        initial-sleep  (if (pos? initial)  #(sleep initial)  #(do))
        interval-sleep (if (pos? interval) #(sleep interval) #(do))]
    (fn delayed [& args]
      (if @started?
        (do
          (interval-sleep)
          (apply f args))
        (try
          (initial-sleep)
          (apply f args)
          (finally
            (reset! started? true)))))))


(defn as-sse-event
  "Given a string, format it such that it can be emitted as a Server-sent Event (SSE).
  See: http://www.w3.org/TR/eventsource/"
  ^String [^String x]
  ;; check whether already in SSE format
  (if (and (.startsWith x "data:")
        (or (.endsWith x \return)
          (.endsWith x \newline)))
    ;; looks like already in SSE format, so return as it is
    x
    (let [n (.length x)
          ^StringBuilder sb (StringBuilder.)]
      ;; the `data:` prefix
      (.append sb "data:")
      ;; detect CR/LF and insert `data:` prefix for next line
      (loop [index 0
             break? false]
        (when (< index n)
          (let [ch (.charAt x index)
                b? (if (or (= ch \return)
                         (= ch \newline))
                     true
                     (do (when break?
                           (.append sb "data:"))
                       false))]
            (.append sb ch)
            (recur (unchecked-inc index) b?))))
      (.append sb \newline)    ; trailing newline
    (.toString sb))))


(defn wrap-sse-event
  "Given function f that returns a string, wrap it such that it always returns a valid Server-sent Event string."
  [f]
  (fn ^String sse-wrapper [& args]
    (as-sse-event (apply f args))))


(defn wrap-fetch-body
  "Given a Ring request handler, return arity-1 fn that accepts request as argument, processes it using the handler,
  ensures the response status is 2xx, fetches the body and returns the same."
  [handler]
  (fn fetch-body [request]
    (let [response (handler request)]
      (if (map? response)
        (let [body (:body response)
              status (:status response)]
          (if (and (integer? status) (< 199 status 300)
                body)
            body
            (throw (IllegalStateException.
                     (str "Cannot understand response: (" (class response) ")" (pr-str response))))))
        response))))


(defn wrap-pst
  "Wrap specified function such that in the event of an exception it prints the stack trace and rethrows the exception."
  [f]
  (fn pst-wrapper [& args]
    (try (apply f args)
      (catch Throwable e
        (.printStackTrace e)
        (throw e)))))
