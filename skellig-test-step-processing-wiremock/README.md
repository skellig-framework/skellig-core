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

### Request matching and stub options

`request.path` accepts an exact path or `path { matches = "/payments/[0-9]+" }`. It excludes the query string. Query parameters, headers and body accept a plain value for equality or a single matcher block:

| Matcher | Meaning |
| --- | --- |
| `value = "text"` | Exact equality |
| `value { contains = "text" }` | Contains the text |
| `value { matches = "text-[0-9]+" }` | Matches the regular expression |
| `value { json = "{\"id\":1}" }` | Equal JSON, ignoring extra fields; array order is significant |

All declared request conditions must match. Missing query parameters or headers do not match.

```
name("Specific payment") {
    wiremock = stub
    stub {
        priority = 1
        request {
            method = POST
            path { matches = "/payments/[0-9]+" }
            query { currency = GBP }
            headers { "X-Token" { contains = secret } }
            body { json = "{\"amount\":10}" }
        }
        response {
            status = 201
            delay = 250
            body = accepted
        }
    }
}
```

`stub.priority` is a positive integer: lower numbers have higher priority, and the default is WireMock's priority of 5. Equal priorities resolve to the latest registered stub. `response.delay` is a non-negative fixed delay in milliseconds and defaults to no delay. This is distinct from a step's top-level retry delay.

The stub id is derived from the server name and the step `id`, so running the step again replaces its earlier stub, and the same `id` on two servers is independent. A step without an `id` gets a random stub id and is never replaced. The step returns `id`, `server` and `port`.

Steps with a `wiremock` key are never handled by the HTTP processor, even if they also carry `url` and `method`.

Reset steps are added in follow-up work. Do not add this module to the Spring Boot performance service runner: it brings Jetty, which must not share a classpath with Tomcat.

## Request journal

`wiremock = requests` returns a detached map with `count` and `requests`. Each request is a map with:

| Field | Value |
| --- | --- |
| `method` | HTTP method |
| `url` | Request path and query string |
| `path` | Request path without the query string |
| `query` | Query parameter names mapped to their first decoded value |
| `headers` | Header names mapped to their first value; names preserve received casing |
| `body` | Raw request body string |
| `loggedTime` | Time received, as milliseconds since the Unix epoch |
| `matched` | Whether a stub matched the request |
| `stubId` | Matched stub ID, or null for unmatched requests |

Omit `request` to read the whole journal, or use a top-level `request {}` block with the same matchers as stubs. Omitted method/path conditions match any method/path. `unmatched = true` restricts the result to requests that matched no stub; it combines with the request filter. Reads do not consume journal entries. The configured journal size limit still applies. No ordering is guaranteed; filter to the requests relevant to the assertion.

```
name("Wait for payment request") {
    wiremock = requests
    server = payments
    request {
        method = POST
        path = "/payments"
    }
    attempts = 20
    delay = 100
    validate { toJson().jsonPath("count") = 1 }
    state { paymentBody = $result.requests.fromIndex(0).body }
}
```

Use `toJson().jsonPath("count")` to validate the count field; bare `count` resolves to the DSL collection function.

`attempts` is the maximum number of journal reads (at least one), with `delay` milliseconds between unsuccessful validations. Polling stops once validation passes; without validation it reads once. A failed validation reports the last journal result. Normal `state {}` and `validate {}` expressions operate on that final result.
