# UI test step processing

Add `skellig-test-step-processing-ui` to your Skellig test project. Its configuration and factory are discovered by the standard processor scan when the configuration contains `ui`.

This first slice supports ordered `navigate` and `click` operations in local Chrome. Other form operations, UI control snapshots, remote browser options, and screenshots belong to the subsequent UI processing tickets. Parallel execution, session limits, and feature-owned contexts are deferred.

```hocon
ui {
  base-url = "http://localhost:8080"
  timeout = 30s
  browser {
    mode = local
    name = chrome
    headless = true
  }
  pages {
    home {
      path = "/"
      elements {
        next = "//*[@id='next']"
      }
    }
  }
}
```

```sts
name("Open the next page") {
    id = nextPage
    ui-actions [
        {
            action = navigate
            page = home
        },
        {
            action = click
            target = "home.next"
        }
    ]
}
```

STS fields inside an action map are separated by newlines; commas separate actions. Quote qualified control references such as `"home.next"`: unquoted dots are property access in the existing value-expression language. Both `ui-actions` and the quoted key `"ui-actions"` are accepted.

Navigation accepts exactly one configured `page` or an absolute HTTP(S) `url`. Page paths can be absolute HTTP(S) URLs or relative to `ui.base-url`. A page without `path` can provide controls but cannot be navigated to. Resolving a control does not navigate to its configured page. Invalid navigation, unknown controls and unsupported actions fail before browser interaction.

Click waits for a unique, visible, enabled control. Zero matches or a control that is not ready are polled until `ui.timeout`, which defaults to 30 seconds. Multiple matches fail immediately. Once the target is ready, the click is attempted once. A failed operation stops the sequence; `attempts` never replays a UI sequence. The result contains the final `url` and is saved using Skellig's usual result key.

The UI processor creates Chrome only when its first UI step executes and reuses that driver until its existing `close()` method is called, normally through `SkelligTestContext.close()`. Navigation, cookies, and authentication persist across steps and sequential scenarios using the same context. Step failures leave the browser open for caller-directed continuation; the processor does not automatically discard or recreate a broken session. Contexts without UI work never create a browser. Repeated cleanup is safe, and closed processors cannot recreate a browser.

Processing and step-runner modules have no feature/scenario lifecycle API. Programmatic callers execute ordinary steps directly and close the owning context in a `finally` block or `use` block. The current JUnit runner closes its context after its complete run. Higher-level callers decide context ownership; one context per feature and parallel execution are future work. UI steps currently require synchronous execution. No explicit reset/quit browser actions are provided.

Chrome runs with a fresh profile. `headless` defaults to false; `mode` and `name` default to local Chrome. Selenium Manager can obtain the driver and Chrome for Testing if missing; its first run can require internet access. Existing Selenium driver configuration remains usable.

Run the browser integration checks with Maven:

```sh
mvn -pl skellig-test-step-processing-ui -am package \
  -Dtest=UiScenarioIT -Dsurefire.failIfNoSpecifiedTests=false
```

Use Maven for builds and verification. The fixture serves deterministic pages from a local HTTP server and runs actual headless Chrome. It verifies standalone execution without grouping hooks, navigation/click ordering, delayed readiness, cross-step and sequential-scenario reuse, browser retention after failure, missing and ambiguous targets, no replay, and ChromeDriver shutdown at context closure.
