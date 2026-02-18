# Why a BPMN Styleguide?

BPMN diagrams are the shared language between business stakeholders and engineering teams.
When a product manager, a developer, and an architect all open the same `.bpmn` file,
they should be able to read it without prior explanation —
because the names, layout, and IDs follow a convention they already know.

## The cost of no conventions

Without agreed-upon rules, BPMN models in a team quickly diverge:

- **Naming drift**: one developer names a task "Send Email", another writes "sendEmail. Both mean the same thing, but
  none is searchable consistently.
- **Unreadable IDs**: auto-generated IDs like `Activity_0xf83kz` in service tasks or message definitions make
  it impossible to understand a process definition without opening a modelling tool.
- **Layout inconsistency**: flows that read right-to-left, crossing sequence flows, elements placed at random
  coordinates — reviewers spend time decoding the diagram instead of reviewing the logic.
- **Automation gaps**: tools like `bpmn-to-code` generate constant names from element IDs. If IDs are
  inconsistent, the generated `ProcessApi` is inconsistent too, and workers reference mismatched type strings.

## What a styleguide gives you

- **Faster onboarding**: a new team member reads the styleguide once and can immediately understand and
  contribute to any process in the repository.
- **Objective reviews**: feedback in process reviews shifts from subjective ("I would have named it
  differently") to rule-based ("the element ID does not follow the `Type_Name` convention — see §2.1").
- **Reliable automation**: code generation, consistency checks (the `/review-process` skill), and static
  analysis all depend on predictable ID and naming patterns. A styleguide is the contract that makes
  this automation trustworthy.
- **Reduced bugs**: type-ID mismatches between a BPMN service task and its Zeebe job worker are a silent
  runtime failure. Consistent naming conventions make these mismatches visible before deployment.

## This styleguide

See [`styleguide.md`](./styleguide.md) for the full set of conventions
covering modeling, technical configuration, and automation patterns used in this project.

> **Note**: Feel free to use this as a template and adapt the styleguide to your own needs!
