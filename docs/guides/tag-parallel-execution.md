# Tag-based parallel execution

Set `skellig.parallel.mode = tag` to submit scenarios to tag-specific worker queues. Each tag stays on the same worker thread throughout the run. Work for a tag executes sequentially in submission order, including across features. Different workers can execute concurrently.

```hocon
skellig.parallel {
    mode = tag
    threads = 4
    tag.groups = ["@payments", "@orders"]
}
```

`threads = 0` uses the available processor count. The scheduler creates at most that many workers. When tags outnumber workers, multiple tag queues share a worker while retaining separate class instances. With one worker, all work runs in submission order. A feature submits its scenarios without waiting for execution, so scenarios from later features can overlap earlier features.

## Selecting a queue

- With `tag.groups`, the first configured tag present on the scenario selects its queue.
- Otherwise, `tag.pattern` can select tags by a full regex match, including `@`, for example `"@group-.+"`. The first matching scenario tag selects the queue.
- With neither setting, the scenario's first tag selects its queue.
- A scenario without a matching tag uses the default queue.

Each scenario runs once. There are no `tag.on-multiple` or `tag.untagged` settings. Only scenarios selected by include/exclude filtering are submitted. Repeated scenario names remain distinct submissions with separate scopes.

## Feature hooks and step blocks

Each queue used by a feature receives this sequence:

1. Matching `@BeforeTestFeature` methods and untagged before-feature methods, in hook order.
2. The “Before Feature” step block, if present.
3. That feature's scenarios assigned to the queue, in declaration order.
4. The “After Feature” step block, if present.
5. Matching `@AfterTestFeature` methods and untagged after-feature methods, in hook order.

An annotated feature hook matches when its tags intersect the tags of any selected scenario of that feature on the queue. An untagged feature hook always matches. Step blocks have no tags of their own and run once on each participating queue. A feature with no selected scenarios queues no hooks or step blocks.

All of this work uses the queue's worker and class instances. Feature setup and teardown share a state separate from scenario state, and every scenario has its own state. `@BeforeTestScenario` and `@AfterTestScenario` methods remain part of scenario execution; feature step blocks do not trigger them.

A failing before-feature hook skips that feature's scenarios and teardown on its queue; other queues continue. A failing “Before Feature” step is reported but allows scenarios to run. Scenario failures still allow teardown. Feature suite and report completion wait for all of the feature's queued work. The test context closes only after all queues and completion callbacks finish.

## Example-table tags

The existing parser accumulates example-table tags. Rows inherit feature tags, scenario tags, their table's tags, and tags from earlier example tables of the same scenario. A later untagged table retains earlier table tags too.

For example, tables tagged `@A`, then `@B`, then an untagged table give their rows tags containing `@A`, then `@A @B`, then `@A @B`. With `tag.groups = ["@B", "@A"]`, the first table's rows use queue A and the later rows use queue B. This does not merge queues or duplicate scenarios.
