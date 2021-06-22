#!/bin/sh
cd /app/core
mvn test
	
cd /app/plugin/cpr
mvn test

cd /app/plugin/geo
mvn test

cd /app/plugin/cpr
mvn test

cd /app/plugin/geo
mvn test

cd /app/plugin/cvr
mvn test

cd /app/plugin/ger
mvn test

cd /app/plugin/eboks
mvn test

cd /app/plugin/prisme
mvn test

cd /app/plugin/subscription
mvn test

cd /app/plugin/combinedPitu
mvn test

cd /app/plugin/statistik
#TODO: We need to solve that tests can only pass in DK-timezone
mvn test -Duser.timezone=Europe/Copenhagen
