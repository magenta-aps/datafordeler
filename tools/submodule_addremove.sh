#!/bin/bash
set -e

SUBMODULES=$(./tools/submodule_check.sh $1)
ADD_SUBMODULES=$(echo "$SUBMODULES" | grep "^<" | cut -f2 -d' ' | grep -v '^$')
REMOVE_SUBMODULES=$(echo "$SUBMODULES" | grep "^>" | cut -f2 -d' ' | grep -v '^$')

echo "Adding:"
while read -r REPO; do
    REPO_PATH=$(echo "$REPO" | tr '-' '/')
    echo "... '$REPO' to '$REPO_PATH' ..."
    git submodule add git@github.com:magenta-aps/datafordeler-${REPO}.git ${REPO_PATH}
done <<< "$ADD_SUBMODULES"

echo "Removing:"
while read -r REPO; do
    REPO_PATH=$(echo "$REPO" | tr '-' '/')
    echo "... '$REPO' ... from '$REPO_PATH' ..."
    echo "NOT IMPLEMENTED!"
    exit 1
done <<< "$REMOVE_SUBMODULES"
