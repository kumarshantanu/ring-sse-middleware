# ring-sse-middleware Change Log

## 0.1.1 / 2017-July-29

- Stop streaming with error message on exception
  - HTTP Kit
  - Manifold
  - Immutant
- Avoid printing stack trace in the generic adapter


## 0.1.0 / 2017-July-07

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
