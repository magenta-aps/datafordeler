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

function subtitle()
{
    title_helper ' ' $@
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

# Output folder
OUTPUT_FOLDER=docs/output/
SPHINX_SOURCE=docs/source/

title "Setup"
function submodule_setup
{
    SUBMODULE_FOLDERS=$(git submodule | sed "s/^ //g" | cut -f2 -d' ' | grep -v "vagrant")
    SUBMODULE_INCLUDES=$(echo "$SUBMODULE_FOLDERS" | sed "s/^/-I /g" | tr '\n' ' ')
}
task "Acquiring submodule information..." submodule_setup

function cleanup_old_documentation
{
    rm -rf $SPHINX_SOURCE/autogen-docs
    rm -rf $SPHINX_SOURCE/autogen-api
    rm -rf .java_sphinx_cache
}
task "Cleaning up old documentation..." cleanup_old_documentation

title "Building reST files from Java API"
function generate_java_api_doc
{
    EMPTY_FOLDER=$(mktemp -d)
    mkdir -p $SPHINX_SOURCE/autogen-api
    cp $SPHINX_SOURCE/javadoc.in $SPHINX_SOURCE/autogen-api/javadoc.rst
    javasphinx-apidoc -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api -v \
        $SUBMODULE_INCLUDES $EMPTY_FOLDER
}
task "Generating java-api documentation..." generate_java_api_doc

title "Building reST files from submodules"
function pull_in_module_docs
{
    mkdir -p $SPHINX_SOURCE/autopulled-docs

    FOLDERS=$(echo -e "localdocs\n$SUBMODULE_FOLDERS")

    AUTOPULLED_MODULES=""
    while read -r FOLDER; do
        FOLDER_PATH="$FOLDER/doc/"

        if [ -d "$FOLDER_PATH" ]; then
            DOC_FOLDER=$SPHINX_SOURCE/autopulled-docs/$FOLDER
            mkdir -p $DOC_FOLDER

            AUTOPULLED_MODULES+="\n   ${FOLDER}/index.rst"
            cp -r $FOLDER_PATH/* $DOC_FOLDER/

            echo "Found doc/ subfolder inside '$FOLDER_PATH'"
        else
            echoerr -e "${YELLOW}\c"
            echoerr "Warning: '$FOLDER' does not contain a doc/ subfolder."
            echoerr -e "${NORMAL}\c"
        fi
    done <<< "$FOLDERS"
}
task "Pulling in module specific documentation..." pull_in_module_docs

function template_module_index
{
    sed "s#{{ MODULES }}#${AUTOPULLED_MODULES}#g" $SPHINX_SOURCE/autopulled-docs.in > $SPHINX_SOURCE/autopulled-docs/index.rst
}
task "Templating module index page..." template_module_index

title "Running sphinx-build to generate documentation"
function generate_html_documentation
{
    mkdir -p $OUTPUT_FOLDER
    sphinx-build -M html $SPHINX_SOURCE $OUTPUT_FOLDER
}
task "Generating html documentation..." generate_html_documentation

function generate_pdf_documentation
{
    PDFLATEX=$(which pdflatex)
    if [ -z "$PDFLATEX" ]; then
        echoerr -e "${RED}\c"
        echoerr "Error: Cannot generate pdf files, without pdflatex."
        echoerr -e "${NORMAL}\c"
        exit 1
    fi
    mkdir -p $OUTPUT_FOLDER
    sphinx-build -M latexpdf $SPHINX_SOURCE $OUTPUT_FOLDER
}
task "Generating pdf documentation..." generate_pdf_documentation

function generate_release_zip
{
    TMP=$(mktemp -d)
    cp -r $OUTPUT_FOLDER/html/ $TMP
    cp -r $OUTPUT_FOLDER/latex/latex/main.pdf $TMP/html/
    ls $TMP/html
    CWD=$PWD
    cd $TMP
    zip -r doc_release.zip html
    cd $CWD
    cp $TMP/doc_release.zip .
}
task "Generating release zip documentation..." generate_release_zip


