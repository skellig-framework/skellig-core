## Skellig 3.0.0

Major release. Changes since 2.1.1:

### Plugins and reporting
- Reports are now implemented as Skellig plugins (#79). Existing Skellig reporting moved into the new `skellig-plugin` module.
- Added a Cucumber report plugin (Skellig results → Cucumber report format).
- Various bug fixes and improvements to the Skellig report; fixed report directory creation on non-Windows platforms.
- Added tests for the reporting plugins.

### Platform and dependencies
- Upgraded dependencies; target JVM bumped to 27 and Kotlin to 2.4 (#82). CI now builds with JVM 27.
- Major version bump to 3.0.0.
- Updated the Cassandra, JDBC and RMQ modules and their integration tests.

### Test step processing
- Changes to `TestStep` / `DefaultTestStep` models, `DefaultTestStepRunner` and `SkelligTestContext`.
- Updates to the `fromCsv`, `toJson` and `jsonTo` value functions.

### Fixes and docs
- More robust TCP channel tests (null-safe teardown, repeated read test).
- Added `../../CONTEXT.md` and a link to the AI-generated technical documentation in the README.

**Full changelog:** https://github.com/skellig-framework/skellig-core/compare/2.1.1...3.0.0
