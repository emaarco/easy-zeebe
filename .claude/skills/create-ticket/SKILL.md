---
name: create-ticket
description: Create or update a GitHub issue for easy-zeebe. Use when the user wants to file a feature request, bug report, or refactor proposal, or update an existing issue.
argument-hint: "[feature|bug|refactor] \"<description>\" | update <issue-number-or-url>"
allowed-tools: Bash(gh *)
---

# Skill: create-ticket

Create or update a GitHub issue for this repository
(feature request, bug report, or refactor task).

## Usage

```
/create-ticket <type> "<title or description>"
/create-ticket update <issue-number-or-url>
```

Where `<type>` is one of: `feature`, `bug`, `refactor`.

## Instructions

1. **Determine mode** — inspect `$ARGUMENTS`:
    - Starts with `update` + an issue number or GitHub issue URL → **update mode**: fetch the issue with
      `gh issue view <number-or-url>` and skip to step 3
    - Otherwise → **create mode**: continue to step 2

2. **Gather information** for the new issue:
    - Extract the issue type (`feature`, `bug`, `refactor`) from `$ARGUMENTS`; if missing, ask the user
    - For `feature`: understand the desired behaviour and why it is needed
    - For `bug`: understand the current vs. expected behaviour and reproduction steps
    - For `refactor`: understand the scope, motivation, and target state

3. **Research** (optional) — if the issue involves a specific library, framework version, API, or
   configuration that you are not fully certain about, ask the user:
   *"Should I search online for [topic] to get accurate details (e.g. exact property names,
   migration guides) before drafting?"*
   If yes, use `WebSearch` / `WebFetch` to collect relevant facts, then incorporate them into the
   draft. Skip this step if you already have sufficient knowledge.

4. **Draft** the issue using the matching template from the Templates section below.

5. **Show and confirm** — present the full draft (create) or the current state + proposed changes (update) and ask:
   *"Proceed? (yes / edit / cancel)"*. Apply edits and show again if requested.

6. **Create or update** using the GitHub CLI:
    - **Create**: `gh issue create --title "<title>" --body "<body>" --label "<label>"`
    - **Update** (use whichever commands apply):
      ```bash
      gh issue edit <number> --title "<title>" --body "<body>"
      gh issue edit <number> --add-label "<label>" --remove-label "<label>"
      gh issue comment <number> --body "<comment>"
      gh issue close <number>
      gh issue reopen <number>
      ```

7. **Report** — run `gh issue view <number>` and show the final issue state with its URL.

---

## Templates

### Feature Request

```
**Title**: [Feature]: <short imperative title>
**Label**: enhancement

**Body**:
## Summary
<One sentence describing the desired feature>

## Motivation
<Why is this feature needed? What problem does it solve?>

## Proposed Solution
<How should the feature work? Include Zeebe/process details where relevant>

## Acceptance Criteria
- [ ] <criterion 1>
- [ ] <criterion 2>
```

### Bug Report

```
**Title**: [Bug]: <short description of the broken behaviour>
**Label**: bug

**Body**:
## Description
<What is going wrong?>

## Steps to Reproduce
1. <step 1>
2. <step 2>

## Expected Behaviour
<What should happen>

## Actual Behaviour
<What actually happens>

## Environment
- Java version:
- Zeebe / Camunda version:
- Module affected:
```

### Refactor

```
**Title**: [Refactor]: <short description of the change>
**Label**: refactor

**Body**:
## Summary
<What will be refactored and why?>

## Current State
<Describe the current implementation and its shortcomings>

## Target State
<Describe what the code should look like after the refactor>

## Out of Scope
<What will NOT be changed>
```
