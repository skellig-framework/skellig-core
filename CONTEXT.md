# Skellig Core

## Purpose

Skellig is a Kotlin/JVM framework for functional, integration, and performance testing with minimal test code. Tests combine readable scenarios (`.skellig`) with reusable step definitions in the Skellig Test Step DSL (`.sts`) or Kotlin/Java methods. Steps generate data, interact with external systems, validate results, and share scenario state. The framework supports HTTP, messaging, databases, remote shell commands, and extensible functions/processors.

## Modules

| Module | Responsibility |
| --- | --- |
| `skellig-feature` | Parses feature files; defines features, scenarios, step references, tags, lifecycle hooks, and execution events. |
| `skellig-test-step-reader` | Reader contract and value-expression model used to represent unevaluated step data. |
| `skellig-test-step-reader-sts` | ANTLR grammars and parsers for `.sts` definitions and DSL value expressions. |
| `skellig-test-step-processing` | Core step models, factories, processors, expression evaluation, built-in functions, validation, task orchestration, and scenario state. |
| `skellig-test-step-runner` | Discovers/registers DSL and annotated method steps; resolves and runs steps by name. `SkelligTestContext` configures and assembles the runtime. |
| `skellig-junit-runner` | JUnit integration for executing features/scenarios through `SkelligRunner` and `SkelligOptions`. |
| `skellig-plugin` | Execution-event plugins, logging, and reports, including Cucumber report output. |
| `skellig-test-step-processing-http` | HTTP request construction, execution, and response handling. |
| `skellig-test-step-processing-ui` | Selenium browser navigation, form interactions, explicit waits, and detached observations for state/validation with a HOCON XPath page catalog and one lazy local Chrome or remote WebDriver session per processor, reused until context cleanup. |
| `skellig-test-step-processing-wiremock` | Embedded WireMock mock servers declared under the `wiremock` configuration key, started with the Skellig context and stopped when it closes. Optional; kept out of the Spring Boot service runner so Jetty and Tomcat never share a classpath. |
| `skellig-test-step-processing-tcp` | TCP channels and send/read/consume operations. |
| `skellig-test-step-processing-rmq` | RabbitMQ/AMQP channels and messaging operations. |
| `skellig-test-step-processing-ibmmq` | IBM MQ connections, queues, and messaging operations. |
| `skellig-test-step-processing-db` | Shared database step models, factories, requests, and processor abstractions. |
| `skellig-test-step-processing-jdbc` | Relational database operations through JDBC, built on the shared DB module. |
| `skellig-test-step-processing-cassandra` | Cassandra operations, built on the shared DB module. |
| `skellig-test-step-processing-unix` | Remote Unix shell execution over SSH. |
| `skellig-test-step-processing-performance` | Repeated/scheduled step execution and duration/message metrics, with built-in and Prometheus implementations. |
| `skellig-performance-junit-runner` | JUnit integration for performance tests. |
| `skellig-performance-service-runner` | Spring Boot web UI/API to start, stop, and monitor performance tests locally or across configured nodes. |

## Execution flow and change locations

`Feature/scenario → step name + parameters → registry lookup → raw definition → factory → typed step → processor → result/validation/state/events`.

`SkelligTestContext` wires readers, registries, factories, processors, configuration, and plugins. Parsing changes belong in the feature or STS reader modules; shared evaluation/validation behavior belongs in core processing; protocol-specific behavior belongs in its adapter module. Kotlin sources live under `src/main/kotlin`; parser grammars live under `src/main/antlr` in the parsing modules. Both Maven and Gradle build definitions are present.

All test step factories must use `convertValue` when reading each property or value from a raw test step: values may be expressions and need proper evaluation against the step parameters and context. Use `convertValue<String>` for string-valued properties instead of calling `.toString()` on raw expressions, and `convertValue<Any>` for other properties before inspecting their types. Map and list expressions evaluate their nested keys and values recursively, so subsequent conversions operate on evaluated values.

## Glossary

- **Feature / scenario:** A named group of tests / one sequence of steps. Example tables supply scenario data; `<parameter>` placeholders substitute it into step names and parameters.
- **Test step:** `org.skellig.feature.TestStep` is a scenario's step reference (name, path, parameters); `org.skellig.teststep.processing.model.TestStep` is an executable step model.
- **STS / raw step:** A `.sts` definition identifies a reusable step by name pattern. The reader represents its properties as `ValueExpression` objects before runtime evaluation.
- **Value expression:** A DSL value, reference, function call, collection, or operation evaluated against an expression context. `${...}` resolves parameters/values and optional fallbacks.
- **Registry / factory / processor:** Finds a step definition / evaluates and constructs its executable model / executes the model and handles its result.
- **Scenario state:** Shared data during a scenario. Step data is stored by step ID and results under `<id>_result`; `get(...)` accesses state.
- **Scenario scope:** `TestScenarioScope` — an id (for logs and reports only), the `TestScenarioState` a step runs against, and the `ParallelGroupContext` of the parallel group the scenario runs in. It travels in the step parameters under the reserved key `skellig.scope` and is copied onto the built test step; processors, tasks and `runTest(...)` use the state of the step's scope, and fall back to the context's default state when a step has none. Without a parallel run, the runner attaches one default scope that wraps the context's only state. Functions called from a step's expressions (properties, validations, state updates, tasks, test data) run with that step's scope: `FunctionValueExecutor.execute(name, value, args, scope)` defaults to the scope-less `execute`, and `get(...)` reads the scope's state and fails when there is no scope. Custom `@Function`s fail without a scope too, as they need its parallel group for their instance.
- **Step log:** The log lines reported for a step. Every scope collects its own (`DefaultTestStepLogger` keeps one buffer per scope id), and a step clears and takes only the buffer of its scope. The report appender attributes a log event to the scope whose id is in the log4j MDC of the thread that logged it (`ScopeLogContext`, key `skellig.scope`), whatever the message says. The runner sets it around a step and around the hooks. Code that hands work to another thread gives the scope to the worker explicitly (the async step pool, `asyncEach`, `asyncForEach` and `runTasksAsyncAndWait`), because a pooled worker keeps the id of the thread that created it; user code that starts its own shared pool or coroutines must do the same, or its lines have no scope. A line with no known scope id is dropped from step logs, except in a run without parallel scenarios, where the default scope collects every line. The `log4j.properties` console patterns of this repository's modules show the thread name (`[%t]`).
- **Parallel group / parallel group context:** `ParallelGroupContext` — a key and the instances of the step definition, hook and `@Function` classes one parallel group uses. An instance is created on first use with the public no-argument constructor, `SkelligTestContextAware` classes then receive the context (`SkelligTestContext.createParallelGroupContext(key)` wires this), and the same instance is returned until the context is closed. Every `TestScenarioScope` refers to the context of its parallel group (`scope.parallelGroup`). The class step registry, the custom function executor and the hook registry keep classes and methods only and fail at startup for a class without a usable constructor; a step, function or hook is called on the instance from `scope.parallelGroup`, so a step method, a hook and a function of one class share an instance within a parallel group and fields carry over between its scenarios. Without a parallel run the context's default scope refers to one default group, so each class has exactly one instance.
- **Validation:** Rules in a step's `validate` section that inspect its execution result using expressions, extractors, and matchers.
- **Task step:** Orchestrates other steps and control flow, including loops, conditions, variables, and state updates.
- **SYNC / ASYNC:** Step execution modes; asynchronous results are delivered through subscriptions.
- **Hook / plugin:** A lifecycle action before/after a feature or scenario / an extension reacting to execution events, such as report generation.
- **Protocol / channel:** The integration selected for a step / its connection or transport abstraction.
- **Mock server:** A named, fixed-port HTTP(S) server owned by the Skellig runtime that stands in for a dependency of the system under test. It is called by external systems; Skellig's HTTP steps are not wired to it automatically. Its lifetime is the whole test context, not a single scenario.
- **Stub:** A rule on a mock server that pairs a request match with a canned response. A stub is owned by the test step that registered it; registering again from the same step replaces the earlier stub. Stubs of other steps may outlive the scenario that created them.
- **Journal:** The mock server's record of requests it received, whether or not any stub matched. Steps read it to assert on what the system under test sent.
- **Performance step:** Repeated execution of test work with timing/load settings and metric collection.
