#!/bin/bash

git config alias.xpush '!git push $1 $2 && jenkins/trigger_build.sh'
