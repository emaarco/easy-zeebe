---
**Date:** 2026-02-25
**Skill:** create-rest-controller
**Skill file:** .claude/skills/create-rest-controller/SKILL.md
**Reference template:** .claude/skills/create-rest-controller/references/rest-controller-template.kt
---

# Skill Review: create-rest-controller

## 1. Frontmatter

| Field            | Value                                                                                                                                                                                                                        | Assessment |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| `name`           | `create-rest-controller`                                                                                                                                                                                                     | Correct    |
| `description`    | "Scaffold or update a REST controller (inbound adapter). Accepts a use-case port file, or a plain description. Use when the user asks to create a REST controller, add an endpoint, or generate an inbound adapter."         | Clear and accurate. Trigger phrases are specific and actionable. |
| `argument-hint`  | `"[<path-to-UseCase-port-file>] [http-verb] [path]"`                                                                                                                                                                        | Matches the actual inputs consumed in the instructions. |
| `allowed-tools`  | `Read, Write, Glob`                                                                                                                                                                                                          | Sufficient for all steps. No tool is listed that is not used. No tool that is needed is missing. |

Frontmatter is complete and consistent.


## 2. Pattern Block

The pattern block shows a minimal, realistic example of a generated controller. It covers:

- `@RestController` + `@RequestMapping` at the class level
- One `@PostMapping` endpoint method
- Nested `data class` DTOs
- A `private fun Form.toCommand()` extension

**Strengths:**
- The snippet is short enough to be immediately readable.
- It covers the single-method-per-controller rule by demonstrating it through example.
- The `toCommand()` extension function with domain type wrapping is shown correctly.

**Gaps:**
- The pattern block does not show the `ResponseEntity<Void>` variant (void use case). That variant is
  present only in the reference template as a comment. Since the IMPORTANT rules mention `ResponseEntity<Void>`
  explicitly, a brief second snippet or a note in the pattern block would help the model choose the right
  return type without needing to read the template first.
- The logger (`KotlinLogging.logger {}`) is shown in the reference template but absent from the inline
  pattern. It is mentioned nowhere in the IMPORTANT rules either. This creates a risk that generated
  controllers omit the logger.


## 3. IMPORTANT Rules

The rules section lists six constraints:

| Rule                                                                          | Assessment |
|-------------------------------------------------------------------------------|------------|
| One endpoint per controller class                                             | Clear and enforceable. Matches the hexagonal single-responsibility intent. |
| Inject the use-case interface as the only constructor parameter               | Clear. Prevents accidental infra leakage into the adapter. |
| Wrap all request fields in domain value objects inside `toCommand()`          | Clear. The "never pass raw strings to the use case" intent is implicit but unambiguous. |
| Always return `ResponseEntity<T>`                                             | Clear. The `Void` vs `Response` distinction is correctly explained. |
| Request/response DTOs are nested `data class` types                           | Clear. |
| Scan domain for value objects before writing                                  | Actionable but incomplete: the rule says "ask the user whether to create it" if a value object is missing, but does not state what to do if the user says no. A "proceed with a raw String and leave a TODO" or "block until resolved" stance would make the fallback deterministic. |

**Missing rule:**
- There is no explicit rule about logging. The template file includes a logger field, but nothing in the
  IMPORTANT section mandates or even recommends it. Consistency across generated files is more reliable when
  the rule lives in the skill, not only in the template.


## 4. Instructions (Step-by-Step)

### Step 1 – Resolve the input source

**Strengths:**
- Handles the most common case (use-case port file) precisely.
- Falls back gracefully by globbing for port files and asking the user.

**Gaps:**
- The step title says "or a plain description" (mirroring the `description` field) but the instructions only
  describe one non-file case: when no argument is provided. The case where a user passes a plain English
  description like "create a controller for subscribing to a newsletter" is never handled. Either the
  argument-hint and description should be narrowed to remove the "plain description" option, or a handling
  rule for it should be added.
- No error path is defined for when the glob returns no results at all. The model is left to improvise.


### Step 2 – Analyze the use-case and controller

**Strengths:**
- Package derivation rule is precise and matches the project convention.
- Source root inference from the file path is explained with a concrete example.

**Gaps:**
- The step heading says "Analyze the use-case and controller" but the body only covers package and path
  derivation. The actual analysis of the use-case interface (method name, `Command` class fields, return type)
  is described in Step 1. This creates a structural inconsistency: the analysis belongs in Step 1 but
  Step 2's heading implies it happens here. Either the heading should be renamed (e.g. "Derive target
  package and file path") or the analysis instructions should be moved here.


### Step 3 – Determine the HTTP verb and path

**Strengths:**
- The verb-inference rules are well-considered and cover the most common use-case name prefixes.
- The ambiguous cases (`Update`, `Delete`) correctly ask the user instead of guessing.
- The concrete URL derivation example is helpful.

**Gaps:**
- The step ends with "Ask the user to confirm or adjust the verb and path before generating." This is a
  blocking interactive step. In automated or non-interactive contexts this would stall. The skill does not
  declare `AskUserQuestion` in `allowed-tools`, which means the model must rely on a plain text reply.
  The `test-process` skill, which has a similar user-confirmation step, declares `AskUserQuestion` in
  `allowed-tools`. This skill should do the same, or the confirmation step should be made optional (only ask
  when inference is ambiguous).


### Step 4 – Generate or update the controller

**Strengths:**
- The class-name derivation rule (`UseCase` → `Controller`) is precise and reversible.
- The generate vs. update branching is explicit.
- "Never leave a TODO for domain-type substitution" is a strong, correct constraint.

**Gaps:**
- The update path says "apply only what is missing or outdated" but does not enumerate what "outdated"
  means. Concrete examples would reduce ambiguity (e.g. "update the return type if the use-case return type
  has changed, update `toCommand()` if new `Command` fields were added").
- Neither the generate nor the update path mentions adding or preserving the logger.


### Step 5 – Report

**Strengths:**
- The three post-generation reminders (verify base path, run `/test-rest-adapter`, run `/create-rest-controller`
  again) are actionable and correctly reference companion skills.

**Gaps:**
- The report step does not specify what to include when a file is skipped. A note like "state why nothing
  changed (e.g. no new fields in the use-case interface)" would make the output more useful.


## 5. Reference Template

**File:** `.claude/skills/create-rest-controller/references/rest-controller-template.kt`

**Strengths:**
- The primary example is production-quality: correct imports, logger, domain type wrapping, and
  `ResponseEntity`.
- The commented-out secondary example (`ConfirmSubscriptionController`) demonstrates the path-variable /
  void variant and is clearly labelled.
- Inline comments on each structural decision (one controller per use case, toCommand wrapping) reinforce
  the IMPORTANT rules without duplication.

**Gaps:**
- The secondary example (path-variable variant) is inside a comment block. If the model needs to generate
  that pattern, it must parse commented-out code, which is less reliable than an active snippet. A separate
  clearly labelled section or a second template file would be cleaner.
- The file imports `java.util.*` (wildcard import). The project likely uses explicit imports consistently.
  This is a minor style inconsistency that could propagate into generated files.
- There is no `GET` endpoint example. Given that the skill's verb-inference rules include `GET` (for
  `Get`, `Find`, `Search`, `List` prefixes), the template should show at least a commented example with
  `@GetMapping` and a `@PathVariable` or `@RequestParam`.


## 6. Cross-Skill Consistency

| Check                                                                 | Result |
|-----------------------------------------------------------------------|--------|
| Companion skill `test-rest-adapter` is referenced in Step 5           | Pass   |
| `allowed-tools` does not include `AskUserQuestion`                    | Inconsistency vs. skills that perform user confirmation (e.g. `test-process`, `create-adr`) |
| Logger pattern matches project convention (`KotlinLogging.logger {}`) | Present in template, absent in IMPORTANT rules |
| Domain-type lookup instruction matches `create-worker` and `automate-process` | Pass — all three skills instruct to scan `domain/` |
| Package derivation convention matches other inbound-adapter skills    | Pass   |
| Post-report reminder to call a test-generation skill matches `create-worker`, `create-process-adapter` | Pass |


## 7. Summary

| Dimension              | Rating   | Notes |
|------------------------|----------|-------|
| Frontmatter            | Good     | Complete, accurate, no excess tools |
| Pattern block          | Adequate | Void variant and logger missing |
| IMPORTANT rules        | Adequate | Logger rule missing; fallback for missing value object is underspecified |
| Step 1                 | Adequate | "Plain description" input unhandled; no error path for empty glob |
| Step 2                 | Adequate | Heading does not match body; analysis belongs in step 1 |
| Step 3                 | Good     | Verb inference is thorough; `AskUserQuestion` tool not declared |
| Step 4                 | Adequate | "Outdated" undefined; logger not mentioned |
| Step 5                 | Good     | Correct companion-skill references; skip reason unspecified |
| Reference template     | Good     | Production-quality; void and GET variants incomplete |
| Cross-skill consistency| Good     | Aligned on domain scan and package convention |

**Overall assessment: the skill is functional and well-structured. It will produce correct output for the
common case (POST with request body, typed use-case port file as input). The main risks are:**

1. **Inconsistent logging** — the logger will appear only when the model happens to follow the template
   closely, because the IMPORTANT rules do not mandate it.
2. **Stalled execution in interactive confirmation (Step 3)** — the `AskUserQuestion` tool is not declared,
   meaning the confirmation step may be handled inconsistently depending on the model's context.
3. **Unhandled "plain description" input** — the description and argument-hint advertise this capability
   but the instructions do not implement it.

**Recommended priority fixes:**
1. Add a logging rule to IMPORTANT.
2. Add `AskUserQuestion` to `allowed-tools` (or make Step 3 confirmation optional/non-blocking).
3. Remove "plain description" from the `description` field and `argument-hint`, or add handling for it in
   Step 1.
4. Clarify what "outdated" means in the update path of Step 4.
5. Add a `GET` example (commented) to the reference template.
