#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
REPO=$(git remote -v | grep "(push)" | cut -f2 | cut -f1 -d' ')
REPOS=$(echo "$REPO" | wc -l)

echoerr() { echo "$@" 1>&2; }

if [ $REPOS -ne 1 ]; then
    echoerr "More than one push repository already."
    echoerr "Please install the Jenkins integration by hand."
    exit 1
fi

SERVER_URL=$(cat "$DIR/JENKINS_SERVER_URL")
REPO_IDENT=$(echo "$REPO" | cut -f2 -d':')

git remote set-url --add --push origin $REPO
git remote set-url --add --push origin git@$SERVER_URL:$REPO_IDENT
