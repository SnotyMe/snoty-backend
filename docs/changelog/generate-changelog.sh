#!/bin/bash

set -euo pipefail

context_file="context.local.json"

# read by Git Cliff
GITHUB_REPO="$(
  git remote get-url origin |
    sed -E 's#^(https?://|git@)[^/:]+[/:]##; s#\.git$##'
)"
export GITHUB_REPO

cliff_args=("$1" --tag "${2:-}" --use-branch-tags --repository ../..)
if [ "${EXPLICIT_REPO:-false}" = "true" ]; then
	git-cliff --context -o "$context_file" "${cliff_args[@]}"
	new_context="$(jq '.[].extra.explicit_repo = true' "$context_file")"
	echo "$new_context" > "$context_file"
	cliff_args=(--from-context "$context_file")
fi

git-cliff "${cliff_args[@]}"

rm "$context_file"
