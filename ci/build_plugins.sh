#!/bin/sh
for plugin in "cpr" "cvr" "ger" "geo" "gladdreg" "adresseservice" "eboks" "prisme" "statistik";
do
  cd $plugin
  mvn --batch-mode -T 2C -DskipTests clean install || exit 1
  cd ..
done
