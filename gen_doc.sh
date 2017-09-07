#!/bin/bash

OUTPUT_FOLDER=docs/output/
SPHINX_SOURCE=docs/source

SUBMODULE_FOLDERS=$(git submodule | sed "s/^ //g" | cut -f2 -d' ')
SUBMODULE_INCLUDES=$(echo "$SUBMODULE_FOLDERS" | sed "s/^/-I /g" | tr '\n' ' ')

EMPTY_FOLDER=$(mktemp -d)

echo "Cleaning up from previous build"
# rm -rf $SPHINX_SOURCE/autogen-api

echo "Building reST files from Java API"
mkdir -p $SPHINX_SOURCE/autogen-api
javasphinx-apidoc -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api -v \
    $SUBMODULE_INCLUDES $EMPTY_FOLDER

echo "Building reST files from submodules"
mkdir -p $SPHINX_SOURCE/autopulled-docs

AUTOPULLED_MODULES=""
while read -r FOLDER; do
    FOLDER_PATH="$FOLDER/doc/"

    if [ -d "$FOLDER_PATH" ]; then
        DOC_FOLDER=$SPHINX_SOURCE/autopulled-docs/$FOLDER
        mkdir -p $DOC_FOLDER

        AUTOPULLED_MODULES=$(echo -e "${AUTOPULLED_MODULES}\n${FOLDER}/index.rst")
        AUTOPULLED_MODULES=${FOLDER}/index.rst
        cp $FOLDER_PATH/* $DOC_FOLDER/

        #LENGTH=${#FOLDER}
        #DIVIDER=$(printf "%-${LENGTH}s" "-")
        #echo $FOLDER
        #echo "${DIVIDER// /-}"
        #ls $FOLDER/doc/
        #echo ""
    else
        echo "'$FOLDER' does not contain a doc/ subfolder."
    fi
done <<< "$SUBMODULE_FOLDERS"

echo $AUTOPULLED_MODULES

sed "s#{{ MODULES }}#${AUTOPULLED_MODULES}#g" $SPHINX_SOURCE/autopulled-docs.in > $SPHINX_SOURCE/autopulled-docs/index.rst

echo "Running sphinx-build -M for html and pdflatex"
mkdir -p $OUTPUT_FOLDER
sphinx-build -M html $SPHINX_SOURCE $OUTPUT_FOLDER
# TODO: Reenable pdf generation
# sphinx-build -M latexpdf $SPHINX_SOURCE $OUTPUT_FOLDER
