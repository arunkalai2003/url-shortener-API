# AI-Assisted Engineering Playbook

This folder is the reusable engineer-led AI workflow for this repository and future projects. The purpose is not to prove that AI can generate code. The purpose is to prove that an engineer can convert requirements into safe, reviewable, production-ready changes while using AI as an accelerator.

## Mandatory lifecycle for every change

1. **Intake** — capture the original requirement without silently filling gaps.
2. **Engineer decisions** — identify business, architecture, security, data, reliability, and operability decisions that require human ownership.
3. **Classify the change** — Greenfield, Brownfield, Bluefield, or Ambiguous.
4. **Acceptance criteria** — convert the requirement into observable behavior.
5. **Tests first** — define new/changed tests before implementation.
6. **AI execution** — record prompts, generated suggestions, edits, rejections, and rationale.
7. **Quality gates** — compile, unit, integration, concurrency, security, performance, and resilience checks as applicable.
8. **Review and sign-off** — the engineer explicitly owns correctness and production readiness.

## Change classifications

- **Greenfield:** new capability/system with no pre-existing implementation constraints.
- **Brownfield:** enhancement, bug fix, refactor, schema/API change, or behavior change in an existing implementation.
- **Bluefield:** new design introduced alongside or inside an existing system where some legacy contracts/data must be preserved while selected components are replaced or modernized.
- **Ambiguous:** requirement cannot be safely implemented until unresolved business or technical choices are recorded.

## Rule: no implementation before decision gates

If a requirement materially changes data identity, API behavior, security/privacy, availability, consistency, retention, concurrency, billing, or user-visible behavior, create/update a decision record first.

## Rule: every requirement changes tests

A requirement is not considered complete because code compiles. Every functional requirement must map to at least one acceptance criterion and at least one test or explicit validation step. Risk-driven requirements can require several tests (e.g., concurrency + integration + failure injection).

## Rule: AI output is never self-approving

AI-generated code may be **Accepted**, **Edited**, or **Rejected**. Every high-impact change requires engineer review and an evidence-based validation result.
