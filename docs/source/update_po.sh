#!/bin/bash

POT_FOLDER=../output/gettext/

sphinx-build -b gettext . $POT_FOLDER
sphinx-intl update -p $POT_FOLDER -l da -l kl -l en
