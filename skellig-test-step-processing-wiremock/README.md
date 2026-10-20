# WireMock test step processing

Add `skellig-test-step-processing-wiremock` to your Skellig test project. Its configuration is discovered by the standard processor scan and is active only when the configuration contains `wiremock`.

Each mock server has a required unique `name` and a required unique fixed `port` (port 0 is rejected). Servers listen as soon as the Skellig context is created. Closing the context resets all stubs and frees the ports. Startup fails with a message naming the server when a name or port is duplicated or a port is already in use.

```hocon
wiremock {
  servers = [
    { name = payments, port = 8081 }
    { name = identity, port = 8082, journal { max-entries = 1000 } }
  ]
}
```

`journal.max-entries` optionally caps the number of requests each server remembers.

## Stubs

A `wiremock = stub` step registers a stub on a mock server. The `server` key is optional only when exactly one server is configured; an unknown name fails with the configured names listed. The `stub` block supports `method`, `path`, response `status`, `headers` and `body`.

```
name("Payment accepted") {
    wiremock = stub
    server = payments
    id = payment-accepted
    stub {
        request {
            method = POST
            path = "/payments"
        }
        response {
            status = 201
            headers { "Content-Type" = "application/json" }
            body = "{\"status\": \"ok\"}"
        }
    }
}
```

The stub id is derived from the server name and the step `id`, so running the step again replaces its earlier stub, and the same `id` on two servers is independent. A step without an `id` gets a random stub id and is never replaced. The step returns `id`, `server` and `port`.

Steps with a `wiremock` key are never handled by the HTTP processor, even if they also carry `url` and `method`.

Request-journal and reset steps are added in follow-up work. Do not add this module to the Spring Boot performance service runner: it brings Jetty, which must not share a classpath with Tomcat.
