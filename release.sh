#!/bin/bash

git submodule deinit -f vagrant/
git rm -r vagrant/

git submodule deinit -f ansible/roles/vim
git rm -r ansible/roles/vim

sed -i "s#git@github.com:#https://github.com/#g" .gitmodules
