#!/bin/bash

## Usage:
# ./vagrant_cmd.sh "COMMAND"
#
## Example:
# ./vagrant_cmd.sh "sudo su -c 'cat /root/gen_and_serve.sh'"

rm -f .vagrant_output
#echo "Running '$@'"
vagrant ssh -c "$@ > /vagrant/.vagrant_output" 2>/dev/null
cat .vagrant_output
rm -f .vagrant_output
