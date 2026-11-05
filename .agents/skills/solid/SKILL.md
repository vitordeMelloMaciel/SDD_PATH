---
name: solid
description: Review, plan, or refactor object-oriented Java code using SOLID when the user asks for a SOLID review, design assessment, or scoped refactor. Use evidence from the current code and avoid speculative abstractions.
---

# SOLID

Use SOLID as a diagnostic tool for this repository, not as a requirement to add patterns or interfaces.

## Context first

1. Read `AGENTS.md`, `rules/restrictions.md`, the active spec and its `plan.md` and `tasks.md` when they exist.
2. Read the relevant Java code and tests before judging the design.
3. Respect `docs/adr/001-stack.md` and `docs/adr/002-design-system.md`. If a proposed correction conflicts with an ADR or requires a new architectural decision, describe it and stop.

## Review method

Evaluate only principles supported by concrete evidence:

- **Single Responsibility:** identify classes that change for unrelated business or technical reasons.
- **Open/Closed:** look for repeated conditional modification caused by a real extension point; do not predict hypothetical variants.
- **Liskov Substitution:** verify that implementations preserve the contract, errors and invariants of the abstraction.
- **Interface Segregation:** identify consumers forced to depend on methods they do not use.
- **Dependency Inversion:** check whether business rules depend directly on volatile infrastructure instead of a stable boundary.

For every finding, cite the file and line, explain the concrete maintenance or behavior risk, name the affected principle and propose the smallest useful correction. Do not report a principle merely because a class is large or an interface has one implementation.

## Implementation mode

When the user asks to implement an approved task:

1. Keep the change inside the active task and its acceptance criteria.
2. Introduce an abstraction only when it isolates a real external dependency, removes meaningful duplication or represents an established domain variation.
3. Prefer Spring constructor injection and existing project conventions.
4. Preserve observable behavior unless the spec explicitly changes it.
5. Add or adjust tests around the behavior being protected, including negative authorization tests when relevant.
6. Finish with `rules/checks.md` and report which `CA-xx` was satisfied.

## Do not

- Do not create one interface per class, empty layers, generic repositories or factories without a demonstrated variation.
- Do not move responsibilities only to make files shorter.
- Do not introduce a dependency, migration, ADR or product decision under the label of SOLID.
- Do not refactor unrelated code while implementing a feature.

## Output

For a review, lead with actionable findings ordered by severity. If there is no concrete SOLID violation, say so and identify only the remaining test or design risks. For a plan, map each proposed change to the problem it solves and to the affected acceptance criterion.
