# Parallel test execution

The JUnit 4 `SkelligRunner` supports three modes: `none`, `feature` and `scenario`. Parallel execution is opt-in. Start with `feature` when scenarios in a feature share resources; use `scenario` only when every scenario is independent.

## Configure a run

Add this to the HOCON file selected by `@SkelligOptions(config = "test.conf")`:

```hocon
skellig.parallel {
    mode = feature
    threads = 4
}
```

No annotation changes are needed. Defaults apply even without a configuration file. These are all supported parallel settings:

| Key | Default | Meaning |
| --- | --- | --- |
| `skellig.parallel.mode` | `none` | `none`, `feature` or `scenario` (case-insensitive). |
| `skellig.parallel.threads` | `0` | Maximum worker count; `0` uses the available processor count. Negative or non-numeric values fail at startup. |

System properties override the configuration file, for example these JVM options:

```text
-Dskellig.parallel.mode=scenario -Dskellig.parallel.threads=4
```

To disable parallel execution in CI, use `-Dskellig.parallel.mode=none`. Invalid modes fail when the runner is constructed and list the supported values. `tag` is not a mode; `skellig.parallel.tag.*` and `skellig.parallel.exclusive-tags` are not supported and provide no scheduling guarantees. Ordinary tags still select tests; they do not serialize them.

## Modes and parallel groups

A parallel group owns the instances of step definition, hook and custom function classes used by its scenarios. It is not a connection pool or a lock on external resources.

| Mode | Execution order | Class instance lifetime |
| --- | --- | --- |
| `none` | Features and scenarios run sequentially. | One default group for the run; fields can carry across features and scenarios. |
| `feature` | Features can overlap; scenarios within each feature run in declaration order. | One group per feature; its feature hooks and scenarios share class instances. |
| `scenario` | Scenarios, including those in different features, can overlap. | One group per scenario; feature setup and teardown have their own group. |

Instances are created lazily using a public no-argument constructor. Step methods, hooks and `@Function` methods on the same class share its group instance. Static fields, Kotlin objects and external services remain shared: scenario isolation does not make them thread-safe. A worker limit of one prevents overlap but does not change the selected mode's state or instance lifetimes.

In parallel modes every scenario has its own `TestScenarioState`, cleaned when the scenario ends. Feature setup and teardown share a separate state. Scenario state does not carry data from one scenario to another, even in `feature` mode. In `none`, the existing default state is reused and cleaned between scenarios.

## Keep shared resources sequential

Skellig does not add per-group connections, locks or pools to connection-holding processors. In `feature` mode, put all scenarios sharing a resource into the same feature and ensure no other concurrently running feature uses it. If that cannot be guaranteed, use a separate sequential run. In `scenario` mode, placing scenarios in the same feature does **not** serialize them.

| Resource | Required grouping |
| --- | --- |
| JDBC | Scenarios using the same database server belong in one feature group. |
| RabbitMQ | Scenarios sharing a channel or queue belong in one feature group. |
| TCP | Scenarios sharing a channel belong in one feature group. |
| Unix/SSH | Scenarios using the same SSH host belong in one feature group. |
| IBM MQ | All IBM MQ scenarios belong in one feature group; queue manager caches and `MQEnvironment` are JVM-wide. |
| UI | All UI scenarios belong in one feature group; the processor shares one browser. |
| WireMock | Scenarios sharing stubs, journal assertions or resets belong in one feature group, or a separate sequential run. |
| Shared queues / performance steps | Keep competing scenarios in one feature group; use a separate sequential run if they must not overlap with any other work. |

For example, run `payments` and `catalogue` features concurrently only if their connections and mutable data are independent. All scenarios using the payments database stay inside the payments feature. Separate features do not isolate a JVM-wide resource.

`inc(...)` is not safe in parallel runs. Put scenarios using it in a separate run with `skellig.parallel.mode=none`, and run that suite after the parallel suite has completed. This also works for performance measurements that require no competing load. There is no exclusive-tag phase: setting a tag or an `exclusive-tags` key does not prevent overlap. The performance runners retain their existing execution model; this configuration controls the functional JUnit runner.

## Inject scenario state into user code

Use an optional `TestScenarioState` or `TestScenarioScope` parameter in a step method instead of obtaining state from the shared test context:

```kotlin
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.function.Function
import org.skellig.teststep.runner.annotation.TestStep

class BookingSteps {
    @TestStep(name = "Remember booking (.+)")
    fun remember(value: String, state: TestScenarioState) {
        state.set("booking", value)
    }

    @TestStep(name = "Check booking (.+)")
    fun checkBooking(scope: TestScenarioScope, expected: String) {
        check(scope.state.get("booking") == expected)
    }

    @Function
    fun booking(state: TestScenarioState): Any? = state.get("booking")
}
```

Include the class's package in `@SkelligOptions(testSteps = [...])`. Injected parameters can appear at any position and do not consume captured step arguments or function arguments: call `booking()` from an STS expression. Scenario hooks can also receive state or scope; feature hooks receive the feature scope. Hooks without parameters continue to work.

`getTestScenarioState()` is deprecated and throws in parallel modes. `SkelligTestContextAware` still supplies shared configuration, registries and services. When starting a child step from custom code, pass the current scope to `TestStepRunner.run(name, parameters, scope)`. Do not cache scenario state in shared fields. Built-in async steps and task functions propagate the caller's scope; custom asynchronous code must carry it explicitly. Third-party processors must use the step's scope for scenario state; there is no marker-based warning that detects unsafe processors.

In `feature` mode, feature hook fields are visible to scenarios through the same group instance. In `scenario` mode, feature hook fields are **not** visible to scenario instances: setup and teardown share their own instance, and every scenario has another. Use external setup resources with appropriate isolation rather than expecting feature state or fields to be copied into scenarios.

## Feature setup, teardown and failures

Feature hooks and `Before Feature` steps run once before any scenario in that feature starts. `After Feature` steps and hooks run once after its scenarios finish, including their awaited asynchronous step results. In `scenario` mode, setup and teardown share the feature's scope and class instances, while scenarios use their own.

The current failure rules are:

- A failing before-feature hook reports a failure and skips that feature's scenarios and teardown.
- A failing `Before Feature` step is reported, but does not skip the feature's scenarios.
- Ordinary scenario/step failures are reported; other scenarios and feature teardown still run.
- A failing after-feature hook reports a failure. Abnormal runner exceptions or a stopped run can bypass teardown; it is not an unconditional cleanup guarantee.

Keep these rules in mind when choosing where to allocate and release external resources.
