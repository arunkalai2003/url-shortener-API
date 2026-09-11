# New Requirement Workflow

Use this file as the repeatable process for every new feature or behavior change.

## 1. Requirement
Paste the requirement exactly as received.

## 2. Classification
- [ ] Greenfield
- [ ] Brownfield
- [ ] Bluefield
- [ ] Ambiguous

## 3. Engineer questions
Document unresolved business and system decisions before asking AI for implementation code.

## 4. Decision record
For each material decision capture:
- Decision
- Alternatives considered
- Chosen option
- Reason
- Risks introduced
- Reversibility/migration impact

## 5. Impact map
List impacted:
- APIs
- services/classes
- database/schema/indexes
- cache
- messaging/events
- auth/security
- observability
- deployment/configuration
- documentation

## 6. Acceptance criteria
Every criterion must be observable and testable.

## 7. Tests before code
Add test cases to the change-specific test plan before implementation.

## 8. AI task contract
Prompt AI with:
- intent
- technical context
- constraints
- non-goals
- acceptance criteria
- relevant existing contracts
- required tests

## 9. Implementation review
Classify AI output:
- Accepted
- Edited
- Rejected

Record rationale for edited/rejected high-impact suggestions.

## 10. Validation
All required gates must pass before sign-off.

## 11. Engineer sign-off
The engineer confirms correctness, maintainability, security, operability, and production-readiness limitations.
