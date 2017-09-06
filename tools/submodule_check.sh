#!/bin/bash

REPOS_TMP=$(mktemp)
REPOS=$(./tools/find_dafo_repos.sh $1 | sed "s/^datafordeler-//g" | sort)

CURRENT_SUBMODULES_TMP=$(mktemp)
CURRENT_SUBMODULES=$(git submodule | sed "s/^ //g" | cut -f2 -d' ' | tr '/' '-' | sort)

echo "$REPOS" > $REPOS_TMP
echo "$CURRENT_SUBMODULES" > $CURRENT_SUBMODULES_TMP

diff $REPOS_TMP $CURRENT_SUBMODULES_TMP
