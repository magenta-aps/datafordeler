#!/bin/bash

cd vagrant/
vagrant destroy
PLAYBOOK='demo.yml' vagrant up
vagrant halt
vagrant up
IFCONFIG=$(./vagrant_sudo.sh 'ip addr show eth0')
IP=$(echo "$IFCONFIG" | grep -o "inet [0-9]*\.[0-9]*\.[0-9]*\.[0-9]*" | cut -f2 -d' ')
 
echo "Documentation generated, and served at:"
echo -e "\t http://$IP:8000/"
cd ..
