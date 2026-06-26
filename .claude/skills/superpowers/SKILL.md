---
name: superpowers
description: >-
  Agentic software-development methodology. Use for ANY non-trivial coding task:
  features, refactors, UX changes, debugging. Enforces design-before-code,
  spec/plan documents, TDD, and verification-before-completion. Even a 1% chance
  of relevance means use it. Triggers: "build", "add feature", "redesign",
  "implement", "fix", "refactor", or whenever work touches more than a one-line change.
---

# Superpowers

Local adaptation of obra/superpowers (https://github.com/obra/superpowers).
Upstream clone was blocked by egress policy in the authoring environment, so this
file captures the methodology faithfully. Project artifacts live in
`docs/superpowers/specs/` and `docs/superpowers/plans/`.

## Priority

1. User's explicit instructions (highest).
2. These skills (override default behavior).
3. Default system prompt (lowest).

If a skill applies, you do not have a choice — use it. Reject rationalizations
like "this is too simple", "I need context first", "the skill is overkill".

## Workflow (run in order)

### 1. Brainstorm — HARD GATE
Do NOT write code, scaffold, or take implementation action until you have
presented a design and the user has approved it.
- Explore existing project context (files, docs, prior specs/plans).
- Ask clarifying questions one at a time when the answer changes the design.
- Present 2-3 alternative approaches with trade-offs; recommend one.
- For UI work, offer visual mockups/comparisons when they genuinely help.
- Get explicit approval before moving on.

### 2. Write the spec
Document the approved design in `docs/superpowers/specs/YYYY-MM-DD-<name>-design.md`:
Summary, Goals, Non-Goals, Risks, Architecture, component boundaries.
Each component has one purpose and a well-defined interface. Self-review for
inconsistency/ambiguity, then ask the user to review.

### 3. Write the plan
Document a task-by-task plan in
`docs/superpowers/plans/YYYY-MM-DD-<name>-plan.md` using `- [ ]` checkboxes.
Reference exact files. Keep tasks small and independently testable.

### 4. Execute with TDD
For each task: write a failing test → minimal code to pass → refactor.
Keep the plan checkboxes updated. Run the project's tests/linters per task.
Never mark a task done on unverified code.

### 5. Verify before completion
Before claiming done: build passes, tests pass, the change actually does what
was asked (run/observe when possible), no debug cruft left, plan fully checked.
State outcomes faithfully — if a step was skipped or a test failed, say so.

### 6. Finish the branch
Commit with clear messages, push to the working branch. Open a PR only if the
user asked for one.
