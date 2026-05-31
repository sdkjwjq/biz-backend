# Performance Flow Design

This document records the confirmed rules for the performance module refactor.

## Data Model

- `biz_performance` stores the overall performance indicator.
- `biz_performance_year` stores the yearly split for one performance indicator.
- `rel_task_performance` links a task to a performance indicator and a specific performance year.

The yearly value is the direct business object for yearly reports. The overall
performance value should be recalculated from all yearly values.

## Automatic Performance

Automatic performance is driven by task submissions.

- Task submission counts immediately.
- Automatic performance does not create a performance audit submission.
- A task with `data_type = 0` does not affect performance.
- `biz_performance.data_type = 1`: sum related task values.
- `biz_performance.data_type = 2`: take the maximum related task value.
- `biz_performance.data_type = 0` is not expected in normal data.
- `rel_task_performance.weight` and `contribution_value` are kept for future use, but current calculation uses `task.currentValue`.

Automatic calculation should first update `biz_performance_year.actual_value`,
then update `biz_performance.current_value` from all yearly values:

- `data_type = 1`: sum yearly values.
- `data_type = 2`: take the maximum yearly value.

## Manual Performance

Manual performance indicators are those whose `perf_code` starts with `1.3`,
`2`, or `3`.

- Manual submit fills a yearly actual value, not an increment.
- Submission counts immediately.
- Submission creates a performance audit record and a snapshot.
- The same `perf_id + year` may not have more than one active submission.
- Only `leaderId` and admin may submit or modify manual performance.
- `auditorId` and `principalId` may view, but may not submit.

Manual audit flow:

```text
submitter submits
  -> flow_status = 10, current_handler_id = auditorId

auditorId approves
  -> flow_status = 20, current_handler_id = admin

admin approves
  -> flow_status = 30
```

Rejection returns directly to the submitter:

```text
10 rejected -> -10
20 rejected -> -20
```

Withdrawal is allowed before final approval:

```text
active submission withdrawn -> 0
```

Rejection and withdrawal restore performance values from snapshots.

## Snapshot

`biz_performance_audit_snapshot` stores values needed to restore a manual
performance submission:

- previous `biz_performance.current_value`
- previous `biz_performance.update_time`
- previous `biz_performance_year.actual_value`
- previous `biz_performance_year.target_value`

## Visibility

Performance visibility must be enforced by the backend.

- Admin can view all performance indicators.
- Non-admin users may only view indicators where they are:
  - `leaderId`
  - `auditorId`
  - `principalId`

Frontend visibility is only a convenience layer.

## Frontend

- Performance page shows only backend-visible records.
- Automatic indicators show refresh and related task details.
- Manual indicators show yearly actual-value submit controls only when the current user is `leaderId` or admin.
- Audit center should include a filter for task audit and performance audit.
- Performance audit notifications are required for submit, pass, reject, and final approval.
