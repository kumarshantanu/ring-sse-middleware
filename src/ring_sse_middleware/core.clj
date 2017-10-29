;   Copyright (c) Shantanu Kumar. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.


(ns ring-sse-middleware.core
  (:require
    [ring-sse-middleware.wrapper :as w])
  (:import
    [java.util.concurrent Semaphore]))


;; ----- Server-sent Events -----


(def sse-empty-response {:status 200
                         :headers {"Content-Type" "text/event-stream;charset=UTF-8"
                                   "Cache-Control" "no-cache, no-store, max-age=0, must-revalidate"
                                   "Pragma" "no-cache"}})


;; ----- request matching functions -----


(defn uri-match
  "Return true if request HTTP method is GET and the URI matches the specified URI, nil otherwise."
  [^String uri request]
  (when (and (= :get (:request-method request))
          (= uri (:uri request)))
    request))


(defn uri-prefix-match
  "Given a prefix string token and a Ring request map, check whether the HTTP method is GET and whether the URI begins
  with the specified prefix. Return a modified request with prefix-stripped URI if there is a match, nil otherwise."
  [^String uri-prefix request]
  (when (= :get (:request-method request))
    (let [^String request-uri (:uri request)]
      (when (.startsWith request-uri uri-prefix)
        (assoc request :uri (subs request-uri (.length uri-prefix)))))))


(defn uri-suffix-match
  "Given a suffix string token and a Ring request map, check whether the HTTP method is GET and whether the URI ends
  with the specified suffix. Return a modified request with suffix-stripped URI if there is a match, nil otherwise."
  [^String uri-suffix request]
  (when (= :get (:request-method request))
    (let [^String request-uri (:uri request)]
      (when (.endsWith request-uri uri-suffix)
        (assoc request :uri (subs request-uri 0 (- (.length request-uri) (.length uri-suffix))))))))


(defn query-token-match
  "Given a query string token and a Ring request map, check whether the HTTP method is GET and whether the request
  contains the specified query string token. Return the request if there is a match, nil otherwise."
  [^String query-token request]
  (when (= :get (:request-method request))
    (when-let [^String query-string (:query-string request)]
      (when (.contains query-string query-token)
        request))))


;; ----- middleware -----


(defn streaming-middleware
  "Given a Ring handler fn, an event streaming fn and the following optional args, return a wrapped handler that
  responds to matching requests by repeatedly fetching data and emitting Server-Sent Events (SSE) streams at specified
  interval.
  Argument         Type                     Default         Description
  --------         ----                     -------         -----------
  request-matcher  (fn [request]) -> match  query string    fn returning request upon match, nil otherwise
                                            'stream=true'
  empty-response   Ring response map        SSE headers     Ring response map without :body
  chunk-generator  (fn [request]) -> chunk  wrapped handler fn returning next chunk in the stream
  max-connections  long                     10              maximum allowed concurrent conncections"
  ([handler stream-generator]
    (streaming-middleware handler stream-generator {}))
  ([handler stream-generator {:keys [request-matcher empty-response chunk-generator ^long max-connections]
                              :or {request-matcher (partial query-token-match "stream=true")
                                   empty-response  sse-empty-response
                                   chunk-generator (-> handler
                                                     (w/wrap-delay 1000)
                                                     w/wrap-fetch-body
                                                     w/wrap-sse-event
                                                     w/wrap-pst) ; arity-1 fn, accepts Ring request, returns body
                                   max-connections 10}
                              :as options}]
    (let [^Semaphore semaphore (when (pos? max-connections) (Semaphore. max-connections))
          response-503 {:status 503
                        :headers {"Content-Type" "text/plain"}
                        :body (str "503 Service Unavailable.

No available streaming connection. Maximum connection limit is: " max-connections)}]
      (fn streaming-handler [request]
        (if-let [matching-request (request-matcher request)]
          (if (if semaphore (.tryAcquire semaphore) true)
            (stream-generator matching-request
              (fn fetch-formatted-chunk []
                (let [^String chunk (chunk-generator matching-request)
                      ^StringBuilder sb (StringBuilder. chunk)]
                  (.append sb \newline)
                  (.toString sb)))
              {:cleanup #(when semaphore (.release semaphore))
               :headers empty-response})
            response-503)
          (handler request))))))
