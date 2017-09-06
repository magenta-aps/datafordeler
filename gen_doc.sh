#!/bin/bash

OUTPUT_FOLDER=docs/output/
SPHINX_SOURCE=docs/source

SUBMODULE_FOLDERS=$(git submodule | sed "s/^ //g" | cut -f2 -d' ')
SUBMODULE_INCLUDES=$(echo "$SUBMODULE_FOLDERS" | sed "s/^/-I /g" | tr '\n' ' ')

EMPTY_FOLDER=$(mktemp -d)

echo "Cleaning up from previous build"
rm -rf $SPHINX_SOURCE/autogen-api

echo "building reST files from Java API"
mkdir -p $SPHINX_SOURCE/autogen-api
javasphinx-apidoc -c .java_sphinx_cache -o $SPHINX_SOURCE/autogen-api -v \
    $SUBMODULE_INCLUDES $EMPTY_FOLDER

echo "doing sphinx-build -M for html and pdflatex"
mkdir -p $OUTPUT_FOLDER
sphinx-build -M html $SPHINX_SOURCE $OUTPUT_FOLDER
sphinx-build -M latexpdf $SPHINX_SOURCE $OUTPUT_FOLDER
