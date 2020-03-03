#!/bin/sh

# Take a copy of our source
cp -r /vagrant/ /root/src/

# Activate virtualenv
source /root/venv/bin/activate

# Generate our documentation
cd /root/src/
./gen_doc.sh

# Serve the documentation
cd docs/output/html/
python -m SimpleHTTPServer
