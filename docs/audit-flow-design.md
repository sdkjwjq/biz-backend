# Task Audit Flow Design

This document records the agreed backend rules for the task audit flow. It is a working design note for future code changes.

## Scope

- Change backend first.
- Do not change frontend unless backend API changes make it necessary.
- Confirm with the project owner before changing frontend behavior.
- Keep existing database field names such as `leaderId`, `auditorId`, and `principalId`.
- Comments and display wording may be corrected.

## Role Meanings

- `leaderId`: task submitter.
- `auditorId`: professional group auditor. This is the first audit node.
- `principalId`: principal / managing department reviewer. This is the second audit node.
- Admin: final audit node. Current code uses fixed admin ID `110228`.

## Main Audit Flow

The expected flow is:

```text
leaderId submits
  -> flowStatus = 10, currentHandlerId = auditorId

auditorId approves
  -> flowStatus = 20, currentHandlerId = principalId

principalId approves
  -> flowStatus = 30, currentHandlerId = admin

admin approves
  -> flowStatus = 40, currentHandlerId remains admin
```

When admin approves:

- The submitted value becomes the official task `currentValue`.
- If `currentValue >= targetValue`, set task `status = 3` completed.
- If `currentValue < targetValue`, set task `status = 1` in progress.
- The task may be submitted again later if it is not completed.

## Rejection Flow

Current business rule: every rejection returns to the submitter.

```text
10 rejected -> -10, currentHandlerId = submitter
20 rejected -> -10, currentHandlerId = submitter
30 rejected -> -10, currentHandlerId = submitter
```

Implementation should leave room for future step-by-step rejection, for example:

```text
30 rejected -> principalId
20 rejected -> auditorId
10 rejected -> submitter
```

But the current behavior must be direct rejection to the submitter.

When rejected:

- Restore task `status` to `1`.
- Restore task value from a snapshot table, not by subtracting the submitted value.
- Restore level-4 task values from snapshot records when level-4 tasks were submitted.
- Mark the rejected audit record as deleted: `isDelete = 1`.
- Keeping rejected records may be supported later, but current behavior is deletion.

## Submission Rules

- Only `leaderId` may submit, resubmit, or withdraw a task.
- If an undeleted active audit record exists for the task with status `10`, `20`, or `30`, block new submission.
- A task may be submitted unlimited times as long as it is not completed.
- For now, even tasks with `status = 3` are not blocked from submission by the new audit design.
- Submission uses overwrite semantics, not accumulation.

Example:

```text
old currentValue = 60
submitted value = 40
admin approves
final currentValue = 40
```

## Resubmission Rules

- Resubmission after rejection creates a new audit record.
- Do not reuse the old `sub_id`.
- The old rejected record is marked `isDelete = 1`.
- New resubmission starts from `flowStatus = 10`.
- New `currentHandlerId = auditorId`.
- Resubmission is also restricted to `leaderId`.

## Withdrawal Rules

- Withdrawal is allowed.
- Only `leaderId` / original submitter may withdraw.
- Withdrawn audit records are marked `isDelete = 1`.
- Keeping withdrawn records may be supported later, but current behavior is deletion.
- After withdrawal, task `status` becomes `1`.
- Values should be restored from the snapshot table.

## Value Snapshot Requirement

Do not add more snapshot fields to `biz_material_submission`.

Add a dedicated snapshot table instead. The table should preserve values needed to restore state after rejection or withdrawal.

Recommended design:

- One snapshot group per audit submission.
- Table name: `biz_audit_snapshot`.
- Use one generic table for level-3 tasks and level-4 tasks.
- Use `target_type` to distinguish `TASK` and `LEVEL4_TASK`.
- Use `target_id` to store the task ID or level-4 task ID.
- Add a unique constraint on `sub_id + target_type + target_id`.
- Save the task's previous `currentValue`, previous `status`, and previous `comment`.
- Save previous values for every affected level-4 task.
- Link snapshots to `sub_id`.
- Keep snapshots even if the audit record is soft-deleted, so restore and debugging remain possible.

Create the snapshot immediately after creating the audit record, in the same transaction.

## Level-4 Task Rule

- If a level-3 task has level-4 children, the frontend submits the level-4 list.
- If it has no level-4 children, the frontend submits the level-3 task directly.
- Backend should preserve this distinction.
- Level-4 submit/restore must use the same snapshot approach as level-3 tasks.

## Data Type Rule

- `dataType = 0` mainly affects performance calculation.
- It does not change the audit flow itself.
- Task audit should focus on submit, approve, reject, withdraw, resubmit, and value restore.

## Logging And Notification

For each audit operation:

- Create an audit log record.
- Send notice to the next handler or submitter where applicable.
- Preserve `currentHandlerId` as admin after final approval.
- Refresh performance after rejection or withdrawal restores old values.

## Known Current-Code Mismatches

These are expected targets for later backend changes:

- Some comments say first audit is department leader, but the confirmed first node is `auditorId`, the professional group auditor.
- Rejection currently restores task values by subtraction in places; this must be replaced by snapshot restoration.
- Resubmission currently reuses the old audit record; it should create a new audit record.
- Submission currently does not strictly enforce `leaderId` as the only submitter.
- Active audit conflict checks should be explicit.

## Follow-Up Questions

Future questions should be limited to 5 or fewer at a time, and each question should include a recommended option.
