#!/bin/bash

# Helper function to error to stderr
echoerr() { echo "$@" 1>&2; }

if [ $# -eq 0 ]; then
    echoerr "No access token provided!"
    exit 1
fi

# Get dafo repositories from github
REPOS_TMP=$(mktemp)
REPOS=$(./tools/find_dafo_repos.sh $1 | sed "s/^datafordeler-//g" | sort)

# Get dafo repositories from submodules
CURRENT_SUBMODULES_TMP=$(mktemp)
CURRENT_SUBMODULES=$(git submodule | sed "s/^ //g" | cut -f2 -d' ' | tr '/' '-' | sort)

echo "$REPOS" > $REPOS_TMP
echo "$CURRENT_SUBMODULES" > $CURRENT_SUBMODULES_TMP

# Find missing / new submodules
diff $REPOS_TMP $CURRENT_SUBMODULES_TMP
