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
    rm -rf $SPHINX_SOURCE/autogen-api-cvr
    rm -rf $SPHINX_SOURCE/autogen-api-geo
    rm -rf $SPHINX_SOURCE/autogen-api-prisme
    rm -rf $SPHINX_SOURCE/autogen-api-statistik
    rm -rf .java_sphinx_cache
}
task "Cleaning up old documentation..." cleanup_old_documentation

title "Building doc files from Java API"
function generate_java_api_doc
{
    EMPTY_FOLDER=$(mktemp -d)
    mkdir -p $SPHINX_SOURCE/autogen-api
    mkdir -p $SPHINX_SOURCE/autogen-api-cvr
    mkdir -p $SPHINX_SOURCE/autogen-api-geo
    mkdir -p $SPHINX_SOURCE/autogen-api-prisme
    mkdir -p $SPHINX_SOURCE/autogen-api-statistik
    cp $SPHINX_SOURCE/javadoc-cvr.in $SPHINX_SOURCE/autogen-api-cvr/cvr-javadoc.rst
    cp $SPHINX_SOURCE/javadoc-geo.in $SPHINX_SOURCE/autogen-api-geo/javadoc-geo.rst
    cp $SPHINX_SOURCE/javadoc-prisme.in $SPHINX_SOURCE/autogen-api-prisme/javadoc-prisme.rst
    cp $SPHINX_SOURCE/javadoc-statistik.in $SPHINX_SOURCE/autogen-api-statistik/javadoc-statistik.rst

    javasphinx-apidoc -f -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api-cvr -v \
        '/vagrant/plugin/cvr/src/main/java/dk/magenta/datafordeler/cvr/service' $EMPTY_FOLDER

    javasphinx-apidoc -f -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api-geo -v \
        '/vagrant/plugin/geo/src/main/java/dk/magenta/datafordeler/geo' $EMPTY_FOLDER

    javasphinx-apidoc -f -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api-prisme -v \
        '/vagrant/plugin/prisme/src/main/java/dk/magenta/datafordeler/prisme' $EMPTY_FOLDER

    javasphinx-apidoc -f -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api-statistik -v \
        '/vagrant/plugin/statistik/src/main/java/dk/magenta/datafordeler/statistik/services' $EMPTY_FOLDER

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

function printf_new() {
    char=$1
    num=$2
    v=$(printf "%-${num}s")
    echo "${v// /$char}"
}

function pull_in_error_codes
{
    mkdir -p $SPHINX_SOURCE/autopulled-codes

    # TODO: The method of acquiring the error codes should be corrected.
    # NOTE: Preferably error-code / error-message pairs
    EXCEPTION_PATH=core/src/main/java/dk/magenta/datafordeler/core/exception/
    CODES=$(grep -Pzo "getCode().*\{[\S\s]*?\}" -r $EXCEPTION_PATH |\
            tr '\0' '\n' | grep 'return ".*";' |\
            sed 's/.*return "\(.*\)";/\1/g' | sed "s/\r//g")
    
    ERROR_CODES=""
    while read -r CODE; do
        FILE="$CODE.rst"
        FILE_PATH="$SPHINX_SOURCE/autopulled-codes/$FILE"
        echo "Code: $CODE" > $FILE_PATH

        TEXT_LENGTH=${#CODE}
        SPACES=`expr 6 + $TEXT_LENGTH`
        printf_new "=" ${SPACES} >> $FILE_PATH

        # TODO: Pull non-translated error message
        echo "Denne fejlbesked betyder..." >> $FILE_PATH

        ERROR_CODES+="\n   $FILE"

    done <<< "$CODES"

    sed "s#{{ CODES }}#${ERROR_CODES}#g" $SPHINX_SOURCE/autopulled-codes.in > $SPHINX_SOURCE/autopulled-codes/index.rst
}
task "Pulling in error codes..." pull_in_error_codes



declare -a langs=("da" "kl" "en")

title "Running sphinx-build to generate documentation"
function generate_html_documentation
{
    for lang in "${langs[@]}"
    do
       subtitle "Generating: $lang"
       mkdir -p $OUTPUT_FOLDER/html/$lang/
       sphinx-build -D language="$lang" -b html $SPHINX_SOURCE $OUTPUT_FOLDER/html/$lang/
    done
}
task "Generating html documentation..." generate_html_documentation



function generate_openapi_documentation
{
    echo "generate_openapi_documentation"
    mkdir -p $OUTPUT_FOLDER/html/da/openapi
    # THis will overwrite the sphinx-generated placeholder file that is referenced by the TOC
    cp $SPHINX_SOURCE/openapi/openapi.html $OUTPUT_FOLDER/html/da/openapi/
    cp $SPHINX_SOURCE/openapi/openapi.json $OUTPUT_FOLDER/html/da/openapi/

    # THis will overwrite the sphinx-generated placeholder file that is referenced by the TOC
    cp $SPHINX_SOURCE/openapi/prismeapi.html $OUTPUT_FOLDER/html/da/openapi/
    cp $SPHINX_SOURCE/openapi/prismeapi.json $OUTPUT_FOLDER/html/da/openapi/




    # THis will overwrite the sphinx-generated placeholder file that is referenced by the TOC
    cp $SPHINX_SOURCE/openapi/cprapi.html $OUTPUT_FOLDER/html/da/openapi/
    cp $SPHINX_SOURCE/openapi/cprapi.json $OUTPUT_FOLDER/html/da/openapi/


    cp -r $SPHINX_SOURCE/openapi/swagger/ $OUTPUT_FOLDER/html/da/openapi/
}
task "Generating openapi documentation..." generate_openapi_documentation


function generate_pdf_documentation
{
    PDFLATEX=$(which pdflatex)
    if [ -z "$PDFLATEX" ]; then
        echoerr -e "${RED}\c"
        echoerr "Error: Cannot generate pdf files, without pdflatex."
        echoerr -e "${NORMAL}\c"
        exit 1
    fi

    for lang in "${langs[@]}"
    do
        subtitle "Generating: $lang"
        mkdir -p $OUTPUT_FOLDER/latex/$lang/
        sphinx-build -b latex -D language="$lang" $SPHINX_SOURCE $OUTPUT_FOLDER/latex/$lang/
    done
    
    CWD=$PWD
    for lang in "${langs[@]}"
    do
        subtitle "Making: $lang"
        cd $OUTPUT_FOLDER/latex/$lang/
        make
        cd $CWD
    done
}
task "Generating pdf documentation..." generate_pdf_documentation


function generate_release_zip
{
    TMP=$(mktemp -d)
    cp -r $OUTPUT_FOLDER/html/ $TMP
    cp -r $OUTPUT_FOLDER/latex/main.pdf $TMP/html/
    ls $TMP/html
    CWD=$PWD
    cd $TMP
    zip -r docs.zip html
    cd $CWD
    cp $TMP/docs.zip .
}
#task "Generating release zip documentation..." generate_release_zip
