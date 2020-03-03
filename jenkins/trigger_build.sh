#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SERVER_URL=$(cat "$DIR/JENKINS_SERVER_URL")
SERVER_PORT=$(cat "$DIR/JENKINS_SERVER_PORT")
REPO=$(git remote -v | grep "(fetch)" | cut -f2 | cut -f1 -d' ')
REPOS=$(echo "$REPO" | wc -l)

echoerr() { echo "$@" 1>&2; }

if [ $REPOS -ne 1 ]; then
    echoerr "More than one fetch repository"
    echoerr "Not supported."
    exit 1
fi

SERVER="http://${SERVER_URL}:${SERVER_PORT}"

curl "${SERVER}/git/notifyCommit?url=${REPO}"
