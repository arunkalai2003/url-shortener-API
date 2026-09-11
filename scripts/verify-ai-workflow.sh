#!/usr/bin/env bash
set -euo pipefail

required=(
  "docs/ai-assisted-engineering/README.md"
  "docs/ai-assisted-engineering/01-engineer-decisions/DECISION-GATES.md"
  "docs/ai-assisted-engineering/02-greenfield/URL-SHORTENER-GREENFIELD.md"
  "docs/ai-assisted-engineering/03-brownfield/BROWNFIELD-EXAMPLE-CUSTOM-ALIAS.md"
  "docs/ai-assisted-engineering/04-bluefield/BLUEFIELD-EXAMPLE-ANALYTICS-MODERNIZATION.md"
  "docs/ai-assisted-engineering/05-ambiguous/AMBIGUOUS-REQUIREMENT-EXPIRATION.md"
  "docs/ai-assisted-engineering/06-new-requirements/NEW-REQUIREMENT-WORKFLOW.md"
  "docs/ai-assisted-engineering/07-test-first/URL-SHORTENER-TEST-MATRIX.md"
  "docs/ai-assisted-engineering/08-ai-execution/AI-EXECUTION-LOG.md"
  "docs/ai-assisted-engineering/09-quality-gates/QUALITY-GATES.md"
  "docs/ai-assisted-engineering/10-review-signoff/ENGINEER-SIGNOFF.md"
  ".github/pull_request_template.md"
)

missing=0
for file in "${required[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "Missing or empty required AI-engineering artifact: $file" >&2
    missing=1
  fi
done

if [[ "$missing" -ne 0 ]]; then
  exit 1
fi

echo "AI-assisted engineering workflow artifacts verified."
