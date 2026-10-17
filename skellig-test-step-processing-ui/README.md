# UI test step processing

Add `skellig-test-step-processing-ui` to your Skellig test project. Its configuration and factory are discovered by the standard processor scan when the configuration contains `ui`.

Supports ordered `navigate`, `click`, `type`, `clear`, `select`, `check`, and `wait` operations in local Chrome or remote WebDriver sessions. UI control snapshots are available to state and validation. Screenshots belong to a subsequent UI processing ticket. Parallel execution, session limits, and feature-owned contexts are deferred.

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

Form operations use the same readiness policy as click: wait for one visible, enabled control, then perform the interaction once. `type` accepts a string `value` (including empty strings), clears existing content by default, and preserves it with boolean `append = true`. `clear` empties an editable control. `select` targets a native HTML select with `by = text` or `by = value` and a string `value`. `check` targets a checkbox and accepts a boolean `value`; repeated requests for the same state do not toggle it. Unsupported control types are rejected by Selenium or the checkbox handler.

`wait` accepts a qualified `target` and one of these `condition` values:

| Condition | Completes when |
| --- | --- |
| `present` | Exactly one control exists, even if invisible or disabled. |
| `absent` | No control matches. |
| `visible` | One control exists and is displayed. |
| `hidden` | The control is absent or is not displayed. |
| `enabled` | One control exists and is enabled, even if invisible. |

Every wait uses `ui.timeout`, including automatic readiness. Ambiguous matches fail immediately for all conditions, including `hidden` and `absent`. Synchronization never repeats an interaction. All action fields are evaluated through existing parameters/value expressions before browser execution; newly captured UI values cannot feed later actions within the same step.

```sts
name("Log in") {
    ui-actions [
        { action = navigate
          page = login },
        { action = type
          target = "login.username"
          value = ${username} },
        { action = type
          target = "login.password"
          value = ${password} },
        { action = check
          target = "login.remember"
          value = true },
        { action = click
          target = "login.submit" },
        { action = wait
          target = "dashboard.account"
          condition = visible }
    ]
}
```

Define the referenced login/dashboard paths and XPath controls in HOCON. For a native select use an action such as `{ action = select ... }` with `target`, `by`, and `value` on separate lines.

After actions, the result contains the final `url` and a detached `pages` map. Refer to controls as `$result.pages.<page>.<control>` in `state`, or `pages.<page>.<control>` in `validate`:

```sts
name("Capture account") {
    id = account
    ui-actions [
        { action = wait
          target = "dashboard.account"
          condition = visible }
    ]
    state {
        accountName = $result.pages.dashboard.account.text
    }
    validate {
        pages.dashboard.account.text = get(accountName)
        pages.dashboard.account.text.contains("Account") = true
    }
}
```

Each captured control is a `UiElementState` object with `present`, `visible`, `enabled`, `selected`, `text`, and `value`. `value` is the current DOM property, so it reflects edits rather than the original HTML attribute. For an absent control, `present` and `visible` are false and the other properties are null. Multiple XPath matches fail immediately during capture. Capture never waits; use a `wait` action for synchronization.

After all actions finish, their `target` and `xpath` values are collected into a map. Repeated targets are captured once; navigation actions contribute no targets. All six properties are materialized for each target and grouped by page. Only controls referenced by actions are captured, regardless of state or validation expressions. To include another control, add a `wait` action for it with the appropriate condition. Every lookup reads the current document and never navigates to a configured page.

The order remains actions → capture → result storage → state updates → one immediate validation evaluation. Both state and validation read the same materialized data. Results use the existing `<id>_result` key (the step name when `id` is omitted), including references through `get(...)`. Captured values can feed later steps with `get(accountName)`; they cannot feed later actions within the same step. Results and values saved in state remain usable after navigation and context cleanup; they contain no WebDriver or WebElement references. A failed validation retains both the stored snapshot and completed state updates and does not replay actions or recapture observations.

The UI processor creates its browser session only when its first UI step executes and reuses that driver until its existing `close()` method is called, normally through `SkelligTestContext.close()`. Navigation, cookies, and authentication persist across steps and sequential scenarios using the same context. Step failures leave the browser open for caller-directed continuation; the processor does not automatically discard or recreate a broken session. Contexts without UI work never create a browser. Repeated cleanup is safe, and closed processors cannot recreate a browser.

Processing and step-runner modules have no feature/scenario lifecycle API. Programmatic callers execute ordinary steps directly and close the owning context in a `finally` block or `use` block. The current JUnit runner closes its context after its complete run. Higher-level callers decide context ownership; one context per feature and parallel execution are future work. UI steps currently require synchronous execution. No explicit reset/quit browser actions are provided.

## Browser configuration

`ui.browser.mode` defaults to `local`, `name` to `chrome`, and `headless` to false. Local mode supports Chrome only. Remote mode supports `chrome`, `firefox`, `edge`, and `safari`; it requires an absolute HTTP(S) `url` pointing to WebDriver (for example Selenium Grid). `remote-url` is rejected. The same STS scenario works in either mode.

```hocon
ui.browser {
  mode = remote
  url = "http://localhost:4444"
  name = chrome
  headless = true
  version = "stable"
  platform = "linux"
  accept-insecure-certs = true
  page-load-strategy = eager
  arguments = ["--disable-dev-shm-usage"]
  preferences { "download.default_directory" = "/tmp/downloads" }
  window { width = 1280, height = 720 }
  timeouts { page-load = 60s, script = 30s, implicit = 0ms }
  capabilities {
    "se:downloadsEnabled" = true
    "provider:options" { build = "ui-ci" }
    "goog:chromeOptions" { excludeSwitches = ["enable-automation"] }
  }
}
```

| Setting | Selenium mapping and constraints |
| --- | --- |
| `version`, `platform` | `browserVersion`, `platformName`. Remote availability depends on the server; setting a platform does not provision an OS locally. |
| `headless` | Chrome/Edge `--headless=new`, Firefox `-headless`; Safari does not support true. |
| `accept-insecure-certs` | Boolean `acceptInsecureCerts`. |
| `page-load-strategy` | `pageLoadStrategy`: `normal`, `eager`, or `none`. |
| `proxy` | W3C proxy object with `proxyType` (`direct`, `manual`, `pac`, `autodetect`, `system`), plus `httpProxy`, `sslProxy`, `socksProxy`, `socksVersion`, `noProxy` (array), or `proxyAutoconfigUrl` as appropriate. For example `proxy { proxyType = manual, httpProxy = "proxy:8080" }`. `socksProxy` requires an integer `socksVersion` from 0 to 255 (normally 4 or 5). This configures browser traffic, not the connection to the WebDriver endpoint. |
| `binary` | Browser executable path on the machine running the browser. Chrome, Edge, Firefox only. |
| `arguments` | Ordered array of browser command-line arguments. Chrome, Edge, Firefox only. |
| `preferences` | Browser preference object. Quote dotted keys, e.g. `"download.default_directory"`. Chrome/Edge preferences may be structured; Firefox values must be strings, booleans or integers. Safari unsupported. |
| `extensions` | Array of packaged extension file paths for Chrome/Edge. Read and base64-encoded on the Skellig machine, including remote runs. Relative paths resolve against the process working directory. Firefox/Safari extension installation is not supported by this setting. |
| `window.width`, `window.height` | Positive integer outer-window dimensions, both required. Sent with WebDriver's window-rect command after session creation. Session is quit if this setup fails. |
| `timeouts.page-load`, `timeouts.script`, `timeouts.implicit` | HOCON durations translated to W3C milliseconds. Non-negative whole milliseconds only. Implicit defaults to zero; page-load and script retain Selenium/server defaults when absent. |
| `capabilities` | Additional W3C standard or namespaced vendor/provider capabilities, using their native JSON-compatible values. Browser options use `goog:chromeOptions`, `ms:edgeOptions`, or `moz:firefoxOptions` for the selected browser. |

`ui.timeout` remains the independent readiness/explicit-wait timeout (30 seconds by default). A nonzero implicit timeout affects every Selenium element lookup and may lengthen explicit waits and observation capture; keep it at zero for predictable UI wait timing. These mappings follow [Selenium browser options](https://www.selenium.dev/documentation/webdriver/drivers/options/) and [waiting guidance](https://www.selenium.dev/documentation/webdriver/waits/).

Configuration is validated before browser creation. Unknown browser fields, unsupported combinations, and duplicate settings are rejected. `capabilities.browserName` and `capabilities.timeouts` are reserved; use `name` and `timeouts`. Other common capabilities cannot appear alongside their corresponding named setting. Raw browser option fields (`args`, `binary`, `prefs`, `extensions`) cannot duplicate configured fields; raw `args` also conflict with an explicit `headless` setting. Window-size arguments cannot accompany `window`. Unrelated vendor option fields are merged. Arbitrary provider capabilities are forwarded, with final validation by the remote provider. Do not assume every setting works for every browser, driver version, OS, or provider.

Chrome runs with a fresh profile by default. Selenium Manager can obtain the local driver and Chrome for Testing if missing; its first run can require internet access. Existing Selenium driver configuration remains usable. Remote mode uses an already running WebDriver endpoint and does not provision Grid or browsers. Remote browser binaries and preference paths refer to the remote machine; extension files refer to the Skellig machine.

Run the browser integration checks with Maven:

```sh
mvn -pl skellig-test-step-processing-ui -am package \
  -Dtest=UiScenarioIT,UiBrowserSessionFactoryTest -Dsurefire.failIfNoSpecifiedTests=false
```

Use Maven for builds and verification. The fixture serves deterministic pages from a local HTTP server and runs actual headless Chrome. It verifies standalone execution without grouping hooks, navigation/click ordering, form replacement/append/clear, native selects, desired checkbox state, explicit waits, malformed fields, delayed readiness, timeouts and ambiguity for every operation, cross-step and sequential-scenario reuse, browser retention after failure, missing and ambiguous targets, no replay, and ChromeDriver shutdown at context closure.

Configuration tests inspect real W3C new-session requests using a small protocol fixture. A focused remote scenario runs the same STS steps through `RemoteWebDriver` against an actual ChromeDriver service, including cookie reuse, captured state, validation, and cleanup; it does not require a provisioned Grid.
