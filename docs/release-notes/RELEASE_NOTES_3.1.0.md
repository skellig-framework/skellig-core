## Skellig 3.1.0

Minor release adding a new Selenium-based UI test step processor. Changes since 3.0.0:

### New: `skellig-test-step-processing-ui` module
- Scenario-scoped Selenium UI processor for navigation and clicks (#91). The WebDriver is reused until the context is closed, without needing scenario hooks.
- Form interactions and explicit waits; typed values are redacted in action descriptions (#92).
- Capture of UI element state from completed actions, available for state storage and validation (#93).
- Configurable local and remote UI browsers (#94).
- Screenshots captured before and after each UI step, retaining failure diagnostics and evidence (#95).
- Documented and verified end-to-end UI login workflow, with module README and examples (#96).

### Platform and dependencies
- Selenium 4.50.0; further JVM dependency upgrades.

**Full changelog:** https://github.com/skellig-framework/skellig-core/compare/3.0.0...3.1.0
