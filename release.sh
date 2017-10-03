#!/bin/bash

git submodule deinit -f vagrant/
git submodule deinit -f ansible/roles/vim
sed -i "s#git@github.com:#https://github.com/#g" .gitmodules

git rm -r vagrant/
git rm -r ansible/roles/vim

git submodule update --init
