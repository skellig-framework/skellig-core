# Tag-based parallel execution

Set `skellig.parallel.mode = tag` to run groups concurrently. Scenarios within each group run sequentially in feature order, then scenario declaration order, even when a group spans several features. Each scenario has its own state; step, hook, and function instances are shared by scenarios in the same group.

```hocon
skellig.parallel {
    mode = tag
    threads = 4
    tag {
        groups = ["@payments", "@orders"]
        on-multiple = merge
        untagged = shared-group
    }
}
```

`threads = 0` uses the available processor count. With one worker, groups run sequentially in configured order. With multiple workers, start order between groups is not guaranteed.

Instead of `tag.groups`, set `tag.pattern`, for example `"@group-.+"`. The regex matches the whole tag, including `@`. Matching tags are discovered in feature/scenario/tag encounter order. A nonempty `tag.groups` takes precedence over the pattern.

For a scenario carrying multiple group tags, `tag.on-multiple` chooses:

- `merge` (default): join all matching groups before execution. Merging is transitive: scenarios tagged `@A @B` and `@B @C` put A, B, and C in one sequential group. Declaration order is preserved across the merged group.
- `first`: use the first matching entry in `tag.groups`, or the first discovered group when using a pattern.
- `fail`: reject the run during runner initialization, naming the scenario and conflicting tags, before any feature hooks execute.

For scenarios with no matching group tag, `tag.untagged` chooses:

- `shared-group` (default): one sequential group for all untagged scenarios.
- `own-group`: a separate group for each scenario.
- `per-feature`: one sequential group per feature.

Only scenarios selected by the include/exclude tag filter participate in grouping or merging.

## Feature lifecycle

The first group to reach a feature runs its before hooks and “Before Feature” steps; other groups wait for setup to finish. The last scenario to finish runs “After Feature” steps and after hooks. Setup and teardown share a separate feature scope and instances, even when different workers execute them. No worker waits for the entire feature to finish before moving on to another feature.

A failed before-feature hook skips that feature's scenarios and teardown and closes its suite. A failed “Before Feature” step is reported but allows scenarios to run. Scenario failures still allow feature teardown. Feature events surround the scenarios in JUnit and Skellig reports.

## Example-table tags

The existing parser accumulates example-table tags. A row inherits feature tags, scenario tags, its table's tags, **and tags from preceding example tables of that scenario**. A later untagged table retains earlier table tags too. For example, tables tagged `@A`, then `@B`, then an untagged table produce rows with group tags `@A`, `@A @B`, and `@A @B`, respectively. With the default `merge`, A and B therefore become one group; with `fail`, initialization rejects the later rows. Tag mode preserves this parser behavior.
