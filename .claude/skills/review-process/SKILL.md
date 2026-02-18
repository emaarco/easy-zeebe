---
name: review-process
description: Step-by-step instructions for auditing a Zeebe BPMN process and its glue-code for consistency and coverage.
user-invocable: false
allowed-tools: Read, Glob, Grep
---

# Skill: review-process

Review a BPMN process model and its glue-code for consistency, coverage, and correctness. Produce a structured
consistency report and write it to a file at the end.

## Step 1 – Locate and read the BPMN file

Search for the `.bpmn` file in `src/main/resources/bpmn/` matching the process name given. Read the file and extract:

- **Service task types** (`zeebe:taskDefinition type` attribute values)
- **Message names** (`<bpmn:message name="...">` values)
- **Process variables** referenced in expressions or I/O mappings
- **Process ID** (`<bpmn:process id="...">`)
- **Element IDs** for all service tasks, events, and gateways
- **Timer definitions** and boundary event types

## Step 1b – Read the BPMN styleguide

Read `docs/bpmn-styleguide/styleguide.md`. Extract the conventions for:

- **Element ID format**: `Type_Name` in CamelCase (e.g. `serviceTask_SendWelcomeMail`)
- **Message ID format**: `<serviceName>.<state>` — both parts CamelCase (e.g. `newsletter.subscriptionConfirmed`)
- **Type ID format**: `<serviceName>.<elementIdWithoutTypePrefix>` — both parts CamelCase (e.g.
  `newsletter.sendWelcomeMail`)
- **Naming rules**: tasks (verb + noun), events (noun + past tense), gateways (short question)

## Step 2 – Read the ProcessApi file

Locate the ProcessApi file (typically in `adapter/process/`). Read and extract:

- `PROCESS_ID` constant
- All `TaskTypes.*` constants
- All `Messages.*` constants
- All `Variables.*` constants
- All `Elements.*` constants

## Step 3 – Discover and read workers and tests

Locate all files in `adapter/inbound/zeebe/`. For each worker, identify:

- The `@JobWorker(type = ...)` value and which ProcessApi constant it references
- The `@Variable` parameters used

## Step 4 – Read the process out-adapter and tests

Locate the process out-adapter in `adapter/outbound/zeebe/`. For each public method, identify:

- Whether it calls `startProcess` or `sendMessage`
- Which `processId` and `messageName` constants it uses

## Step 5 – Read the process test

Locate the process integration test (typically in `test/.../adapter/process/`). Identify:

- Which process paths are tested (happy path, timer expiry, abort, etc.)
- Which elements are asserted with `hasCompletedElement`

## Step 6 – Produce the consistency report

Output a structured report with these sections:

**Task Type Coverage**: For each service task in the BPMN, report whether a worker exists. Flag BPMN tasks without a
worker and workers without a BPMN task.

**Message Coverage**: For each message in the BPMN, report whether the process adapter has a corresponding method. Flag
gaps.

**ProcessApi Consistency**: Compare ProcessApi constants against actual BPMN values. Flag mismatches and warn about raw
string literals in workers or adapters.

**Variable Coverage**: List variables used in workers and adapters. Verify they appear in `Variables.*` in the
ProcessApi.

**Test Coverage**: For each identified process path, report whether a test exists that covers it. Also report whether
each worker and process-adapter are covered as well

**Styleguide Compliance**: Check the BPMN model against the conventions loaded in Step 1b:

- For each service task, event, and gateway: verify the element ID matches the `Type_Name` CamelCase pattern
- For each message: verify the message name matches the `<serviceName>.<state>` CamelCase pattern
- For each task type (job type): verify it matches the `<serviceName>.<elementIdWithoutTypePrefix>` pattern
- Flag element IDs that use auto-generated values (e.g. `Activity_0xf83kz`) or that deviate from the convention
- Flag task/event names that do not follow verb+noun (tasks) or noun+past-tense (events) conventions

**Summary**: Overall assessment — ✅ consistent / ⚠️ minor gaps / ❌ significant issues — with actionable next steps.

## Step 7 – Write the report to a file

Determine the output file path as `docs/<processName>-process-review.md`, where
`<processName>` is the kebab-case process name derived from the process ID
(e.g. process ID `newsletter-subscription` → file `docs/newsletter-process-review.md`).

Prepend a metadata header to the report:

- `**Date:**` set to today's date
- `**Process file:**` set to the relative BPMN path
- `**Process ID:**` set to the process ID

Write the complete report (header and all sections from Step 6) to the output file.
Confirm the file path to the user.
