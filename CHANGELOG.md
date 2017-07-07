# ring-sse-middleware Change Log

## Todo
- [Todo] Use custom thread-pool (or Clojure's pooled-executor) instead of Clojure's solo-executor in adapters


## [WIP] 0.1.0 / 2017-July-??

- Configurable API support
  - URI based trigger
  - Data source (chunk generator)
  - Maximum connection limit
- Adapters
  - Generic (requires output buffering)
  - HTTP Kit
  - Immutant
  - Manifold (Aleph)
- Web server tests
  - Aleph
  - HTTP Kit
  - Immutant
  - Jetty
