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

## Glossary

- **Feature / scenario:** A named group of tests / one sequence of steps. Example tables supply scenario data; `<parameter>` placeholders substitute it into step names and parameters.
- **Test step:** `org.skellig.feature.TestStep` is a scenario's step reference (name, path, parameters); `org.skellig.teststep.processing.model.TestStep` is an executable step model.
- **STS / raw step:** A `.sts` definition identifies a reusable step by name pattern. The reader represents its properties as `ValueExpression` objects before runtime evaluation.
- **Value expression:** A DSL value, reference, function call, collection, or operation evaluated against an expression context. `${...}` resolves parameters/values and optional fallbacks.
- **Registry / factory / processor:** Finds a step definition / evaluates and constructs its executable model / executes the model and handles its result.
- **Scenario state:** Shared data during a scenario. Step data is stored by step ID and results under `<id>_result`; `get(...)` accesses state.
- **Validation:** Rules in a step's `validate` section that inspect its execution result using expressions, extractors, and matchers.
- **Task step:** Orchestrates other steps and control flow, including loops, conditions, variables, and state updates.
- **SYNC / ASYNC:** Step execution modes; asynchronous results are delivered through subscriptions.
- **Hook / plugin:** A lifecycle action before/after a feature or scenario / an extension reacting to execution events, such as report generation.
- **Protocol / channel:** The integration selected for a step / its connection or transport abstraction.
- **Performance step:** Repeated execution of test work with timing/load settings and metric collection.
