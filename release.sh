#!/bin/bash

git rm -r vagrant/
git rm -r ansible/roles/vim
sed -i "s#git@github.com:#https://github.com/#g" .gitmodules

git submodule update --init
