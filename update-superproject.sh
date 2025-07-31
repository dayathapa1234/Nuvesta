#!/bin/bash

# Get the script location and go to the parent repo
cd "$(dirname "$0")/.."

# Stage submodule folders (safely: only if they exist)
for module in auth-server gateway-service ml-server; do
  if [ -d "$module" ]; then
    git add "$module"
  fi
done

# Commit only if there are changes
if git diff --cached --quiet; then
  echo "No changes to commit in Nuvesta"
else
  git commit -m "Update auth-server submodule pointer to latest commit"
  git push
fi