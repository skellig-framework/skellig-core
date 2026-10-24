## Skellig 3.1.0

Minor release adding a new Selenium-based UI test step processor and a new WireMock mock server step processor. Changes since 3.0.0:

### New: `skellig-test-step-processing-ui` module
- Scenario-scoped Selenium UI processor for navigation and clicks (#91). The WebDriver is reused until the context is closed, without needing scenario hooks.
- Form interactions and explicit waits; typed values are redacted in action descriptions (#92).
- Capture of UI element state from completed actions, available for state storage and validation (#93).
- Configurable local and remote UI browsers (#94).
- Screenshots captured before and after each UI step, retaining failure diagnostics and evidence (#95).
- Documented and verified end-to-end UI login workflow, with module README and examples (#96).

### New: `skellig-test-step-processing-wiremock` module
- Embedded WireMock mock servers declared under the `wiremock` configuration key, listening as soon as the context is created and stopped (ports freed) when it closes.
- `wiremock = stub` registers stubs from a request/response DSL or a raw WireMock JSON mapping; re-running a step replaces its earlier stub.
- `wiremock = requests` reads the request journal for `validate {}` and `state {}`, with filtering, unmatched-only mode and polling through `attempts` and `delay`.
- `wiremock = reset` clears stubs, the journal or both, on one server or all.
- Optional HTTPS, files directory, bind address and journal size limit per server.
- Optional module: it brings Jetty, so do not add it to the Spring Boot service runner. Existing HTTP steps are unaffected.
- WireMock 4.0.0-beta.40; module README with a worked `.sts` example.

### Platform and dependencies
- Selenium 4.50.0; further JVM dependency upgrades.
- Minor change in `TestScenarioRunner` in `skellig-junit-runner`.

**Full changelog:** https://github.com/skellig-framework/skellig-core/compare/3.0.0...3.1.0
