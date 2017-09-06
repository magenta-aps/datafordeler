#!/bin/bash

# Start at page one, pull 30 repos per request
PAGE=1
REPOS_PER_REQUEST=30
ACCESS_TOKEN=$1

# Helper function to error to stderr
echoerr() { echo "$@" 1>&2; }

if [ $# -eq 0 ]; then
    echoerr "No access token provided!"
    exit 1
fi

# Run until, all repos have been retrieved
while true; do
    # Get the summary of repos, by querying github:
    # See: https://developer.github.com/v3/repos/#list-organization-repositories
    RESPONSE=$(curl -H "Authorization: token $ACCESS_TOKEN" -is "https://api.github.com/orgs/magenta-aps/repos?page=$PAGE&per_page=$REPOS_PER_REQUEST")
    # Get the number of repos found
    KEEP_GOING=$(echo "$RESPONSE" | grep "full_name" | wc -l)

    echoerr "Found $KEEP_GOING repositories on request $PAGE"

    DAFO_REPOS=$(echo "$RESPONSE" | grep -Po '"name": "datafordeler-.*?"' | tr -d '"' | cut -f2 -d' ')
    if [ -n "$DAFO_REPOS" ]; then
        echo "$DAFO_REPOS"
    fi

    # If we did not retrieve a full page, we're done
    if [ "$KEEP_GOING" -ne $REPOS_PER_REQUEST ]; then
        break
    fi

    # Get the next page
    PAGE=$(($PAGE+1))
done
