#!/bin/bash

## Colors
##-------
BLACK=$(tput setaf 0)
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
LIME_YELLOW=$(tput setaf 190)
POWDER_BLUE=$(tput setaf 153)
BLUE=$(tput setaf 4)
MAGENTA=$(tput setaf 5)
CYAN=$(tput setaf 6)
WHITE=$(tput setaf 7)
BRIGHT=$(tput bold)
NORMAL=$(tput sgr0)
BLINK=$(tput blink)
REVERSE=$(tput smso)
UNDERLINE=$(tput smul)

## Size
##-----
LINES=$(tput lines)
COLUMNS=$(tput cols)

## Log file
##---------
LOG=gen_doc.log

# Helper functions
#-----------------
function echoerr()
{
    echo $@ 1>&2
}

function echolog()
{
    echo $@
    #echo $@ > $LOG
}

function title_helper()
{
    CHAR=$1
    shift
    CONTENT="$CHAR $* $CHAR"
    BORDER=$(echo "$CONTENT" | sed "s/./$CHAR/g")
    echolog ""
    echolog "$BORDER"
    echolog "$CONTENT"
    echolog "$BORDER"
}

function title()
{
    title_helper "-" "$@"
}

## Task function
##--------------
function task
{
    INFO_STRING=$1
    TEXT_LENGTH=${#INFO_STRING}
    MAX_SPACING=$(($COLUMNS<80?$COLUMNS:80))
    SPACES=`expr $MAX_SPACING - $TEXT_LENGTH`
    printf "$INFO_STRING"
    STDERR_FILE=$(mktemp)
    $2 >> $LOG 2>$STDERR_FILE
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        printf "%${SPACES}s\n" "${GREEN}[OK]${NORMAL}"
        cat $STDERR_FILE
    else
        printf "%${SPACES}s\n" "${RED}[FAIL]${NORMAL}"
        cat $STDERR_FILE
        exit 1
    fi
}

# Clean up
echo "" >> $LOG
echo "" >> $LOG
echo "" >> $LOG
date >> $LOG
echo "-----------------------------"  >> $LOG

title "Deploy a development server at 0.0.0.0:8000"
function start_server
{
    cd docs/output/html/
    python -m http.server
}
task "Serving docs on development server (0.0.0.0:8000)..." start_server
