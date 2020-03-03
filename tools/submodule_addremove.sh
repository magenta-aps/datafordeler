#!/bin/bash

# Helper function to error to stderr
echoerr() { echo "$@" 1>&2; }

if [ $# -eq 0 ]; then
    echoerr "No access token provided!"
    exit 1
fi

# Find all missing / new submodules
SUBMODULES=$(./tools/submodule_check.sh $1)
# Find which are to be added
ADD_SUBMODULES=$(echo "$SUBMODULES" | grep "^<" | cut -f2 -d' ' | grep -v '^$')
# Find which are to be removed
REMOVE_SUBMODULES=$(echo "$SUBMODULES" | grep "^>" | cut -f2 -d' ' | grep -v '^$')

# Add all missing
echo "Adding:"
while read -r REPO; do
    REPO_PATH=$(echo "$REPO" | tr '-' '/')
    echo "... '$REPO' to '$REPO_PATH' ..."
    git submodule add git@github.com:magenta-aps/datafordeler-${REPO}.git ${REPO_PATH}
done <<< "$ADD_SUBMODULES"

# Remove all extra (eventually)
# TODO: Implement this
echo "Removing:"
while read -r REPO; do
    REPO_PATH=$(echo "$REPO" | tr '-' '/')
    echo "... '$REPO' ... from '$REPO_PATH' ..."
    echo "NOT IMPLEMENTED!"
    exit 1
done <<< "$REMOVE_SUBMODULES"
