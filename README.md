# ring-sse-middleware

[Ring](https://github.com/ring-clojure/ring) middleware to stream
[Server-Sent Events](http://www.w3schools.com/html/html5_serversentevents.asp) (SSE) - a lightweight and standardized
technology as part of HTML5 where the client receives automatic push notifications from the server via HTTP connection.

More details on [W3C](http://www.w3.org/TR/eventsource/) and [Wikipedia](https://en.wikipedia.org/wiki/Server-sent_events).


## Usage

Clojars coordinates `[ring-sse-middleware "0.1.3-SNAPSHOT"]`

_Requires Clojure 1.8 or higher._

_**No web server dependency is included. You must add a compatible web server to your project.**_


### Tested web servers

This middleware is tested with the following web servers:

| Web server                                                                             | Version                     |
|----------------------------------------------------------------------------------------|-----------------------------|
| [Aleph/Manifold](http://aleph.io/)                                                     | 0.4.0 to 0.4.3              |
| [HTTP Kit](http://www.http-kit.org/)                                                   | 2.1.x to 2.2.0              |
| [Immutant](http://immutant.org/)                                                       | 2.1.9                       |
| [Jetty](http://www.eclipse.org/jetty/) [adapter](https://github.com/ring-clojure/ring) | 9.2.x (Ring 1.4.0 to 1.6.2) |


### Namespaces

Remainder of this document uses the following namespace aliases:

```clojure
(require '[ring-sse-middleware.core    :as r])
(require '[ring-sse-middleware.wrapper :as w])
(require '[ring-sse-middleware.adapter.generic  :as g])  ; for any server with no response buffering
(require '[ring-sse-middleware.adapter.http-kit :as h])  ; for HTTP-Kit server only
(require '[ring-sse-middleware.adapter.immutant :as i])  ; for Immutant server only
(require '[ring-sse-middleware.adapter.manifold :as m])  ; for Manifold (Aleph server) only
```

### Quickstart with defined route and HTTP Kit server

Let us assume you have
- a Ring handler defined as `handler`
- a URI endpoint `/app/metrics` that returns a JSON string of metrics data
- web server [HTTP Kit](http://www.http-kit.org/)

You want to periodically (at 1 second interval) query this data and stream it to a browser client for visualization.
Use ring-sse-middleware to set up streaming.

```clojure
(def wrapped-handler (-> handler
                       (r/streaming-middleware h/generate-stream))) ; for Aleph it would be m/generate-stream
```

Once you start HTTP Kit, you can fetch the stream from the URI `/app/metrics?stream=true` on the same server. The
default configuration is triggered with query parameter `stream=true` for HTTP GET requests, and has 1 second interval.


### Quickstart with custom options and Jetty server

Let us assume you have
- a Ring handler defined as `handler`
- a no-argument function `emit-score` that returns a comma-separated string of a cricket match score
- web server [Jetty](http://www.eclipse.org/jetty/) using the [Ring Jetty adapter](https://github.com/ring-clojure/ring)

You want to periodically (at 3 seconds interval) query this data at URI `/score` and stream it to a remote client.
Use the ring-sse-middleware to set up streaming for at most 100 clients.

```clojure
(def wrapped-handler (-> handler
                       (r/streaming-middleware g/generate-stream {:request-matcher (partial r/uri-match "/score")
                                                                  :chunk-generator (-> (fn [_] (emit-score))
                                                                                     (w/wrap-delay 3000)
                                                                                     w/wrap-sse-event
                                                                                     w/wrap-pst)
                                                                  :max-connections 100})))
```

Once you start Jetty, you can fetch the stream from the URI `/score` on the server. Be sure to configure Jetty's output
buffering to minimum to instantly receive the stream updates. This example uses generic `g/generate-stream` streaming
function. In theory it should work with all web servers, but in practice it works with only few, often poorly. For
proper Server-sent Events support consider one of the non-generic web servers.


### Development

Run the server (any of the following commands, press Ctrl+C to stop) in one terminal:

```shell
lein do clean, aleph run
lein do clean, http-kit run
lein do clean, immutant run
lein do clean, jetty run
```

Run the client (press Ctrl+C to stop) in another terminal:

```shell
curl -v "localhost:3000/?stream=true"
```


## License

Copyright © 2017-2021 Shantanu Kumar

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
