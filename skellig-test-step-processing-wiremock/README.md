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

Stub, request-journal and reset steps are added in follow-up work. Do not add this module to the Spring Boot performance service runner: it brings Jetty, which must not share a classpath with Tomcat.
