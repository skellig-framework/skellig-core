# UI test step processing

Add `skellig-test-step-processing-ui` to your Skellig test project. Its configuration and factory are discovered by the standard processor scan when the configuration contains `ui`.

This first slice supports ordered `navigate` and `click` operations in local Chrome. Other form operations, UI control snapshots, remote browser options, session limits, and screenshots belong to the subsequent UI processing tickets.

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

The JUnit scenario runner opens a resource scope before scenario hooks and closes it after steps and hooks finish, including failures. Chrome is created only when a UI step executes, shared across that scenario's steps, and quit at scenario completion. Non-UI scenarios never create a browser. Context shutdown also closes outstanding sessions. Programmatic callers must use `startScenario(id)` and `finishScenario(id)` on the `TestStepRunner`, with completion in a `finally` block. UI steps currently require synchronous execution; asynchronous UI scheduling is addressed by the parallel-session ticket.

Chrome runs with a fresh profile. `headless` defaults to false; `mode` and `name` default to local Chrome. Selenium Manager can obtain the driver and Chrome for Testing if missing; its first run can require internet access. Existing Selenium driver configuration remains usable.

Run the browser integration checks with Maven:

```sh
mvn -pl skellig-test-step-processing-ui -am package \
  -Dtest=UiScenarioIT -Dsurefire.failIfNoSpecifiedTests=false
```

For a compatible Gradle/JDK installation, run `:skellig-test-step-processing-ui:integrationTest`. The fixture serves deterministic pages from a local HTTP server and runs actual headless Chrome. It verifies navigation/click ordering, delayed readiness, cookie reuse and fresh scenario sessions, missing and ambiguous targets, no replay, and ChromeDriver shutdown before context closure.
