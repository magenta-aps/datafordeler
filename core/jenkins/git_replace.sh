#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run git command
git "$@"

# If we did a push
while test $# -gt 0
do
    if [ "$1" == "push" ]; then
        # Trigger Jenkins
        $DIR/trigger_build.sh
    fi
    shift
done
