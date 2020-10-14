#!/bin/sh
for plugin in "cpr" "geo" "cvr" "ger" "eboks" "prisme" "subscription" "statistik";
do
  cd $plugin
  mvn --batch-mode -T 2C -DskipTests clean install || exit 1
  cd ..
done
