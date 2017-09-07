#!/bin/bash

vagrant halt
vagrant up
IFCONFIG=$(./vagrant_sudo.sh 'ifconfig eth0')
IP=$(echo "$IFCONFIG" | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}')

echo "Documentation generated, and served at:"
echo -e "\t http://$IP:8000/"
