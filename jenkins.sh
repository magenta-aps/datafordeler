#!/bin/bash
set -e

function title()
{
    CHAR='#'
    CONTENT="$CHAR $* $CHAR"
    BORDER=$(echo "$CONTENT" | sed "s/./$CHAR/g")
    echo ""
    echo "$BORDER"
    echo "$CONTENT"
    echo "$BORDER"
}

title "Listing repository content"
ls

title "Printing working directory"
pwd

title "Listing dockers"
docker ps -a

title "Starting docker + building"
docker run --rm -v $PWD:/srv/ maven:latest bash -c "cd /srv/ && ./jenkins/inside_docker.sh"

