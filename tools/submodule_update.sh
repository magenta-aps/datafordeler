#!/bin/bash

ROOT=$(pwd)

# Helper function to error to stderr
echoerr() { echo "$@" 1>&2; }

# Find all missing / new submodules
SUBMODULES=$(git submodule | sed "s/^ //g" | cut -f2 -d' ')

# Checkout and pull
echo "Checking out:"
while read -r REPO; do
    REPO_PATH=$(echo "$REPO" | tr '-' '/')
    echo "... '$REPO' ..."
    cd $REPO_PATH
    # Fetch everything
    git fetch --all
    # Find development branch name (develop/development)
    DEVELOP_NAME=$(git branch -a | grep "origin/develop" | rev | cut -f1 -d'/' | rev)
    # Checkout the best branch we have
    if [ -z "$DEVELOP_NAME" ]; then
        git checkout master
    else
        git checkout $DEVELOP_NAME
    fi
    # Pull it
    git pull
    cd $ROOT
done <<< "$SUBMODULES"
