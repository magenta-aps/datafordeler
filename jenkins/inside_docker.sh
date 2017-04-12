#!/bin/bash

function title(){ CHAR='*';CONTENT="$CHAR $* $CHAR";BORDER=$(echo "$CONTENT" | sed "s/./$CHAR/g");echo "";echo "$BORDER";echo "$CONTENT";echo "$BORDER";}
function subtitle(){ CHAR=' ';CONTENT="$CHAR $* $CHAR";BORDER=$(echo "$CONTENT" | sed "s/./$CHAR/g");echo "";echo "$BORDER";echo "$CONTENT";echo "$BORDER";}

title "Inside docker"

# Switch to the source directory
# Example:
#   cd src/
# TODO: Insert something here

# Setup the build environment
# Example:
#   title "Setting up build environment"
#   apt-get update ...
#   apt-get install ...
# TODO: Insert something here

# Setup the project
# Example:
#   title "Seeting up project"
#   virtualenv ~/venv/
#   source ~/venv/bin/activate
#   pip install -r requirements.txt
#   python manage.py migrate
# TODO: Insert something here

# Test the project (and collect coverage)
# Example:
#   title "Running tests"
#   echo "TEST_OUTPUT_DIR = 'reports/test/' >> project/settings.py
#   echo "TEST_RUNNER = 'xmlrunner.extra.djangotestrunner.XMLTestRunner'" \
#       >> project/settings.py
#   coverage run --source="." python manage.py test app.tests
#   coverage xml -o reports/coverage.xml
# TODO: Insert something here

# Lint the code
# Example:
#   title "Checking the code"
#   pylint -f parseable app/ | tee reports/pylint.log
#   flake8 app/ | tee reports/pep8.log
# TODO: Insert something here

# Generate documentation
# Example:
#   title "Generating docs"
#   cd docs/ && make html
#   cd ..
#   cp -r docs/_build/html/ reports/docs/
# TODO: Insert something here

# Generate exit status
# Example:
#   exit $TRANSLATE_CHECK && $MISSING_I18N_CHECK && ...
# TODO: Insert something here

