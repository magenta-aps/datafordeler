#!/bin/sh

java -version

TRUST_CACERTS=${TRUST_CACERTS:=""}
IFS=,

# Import TRUST_CACERTS to trust-store
for FILE in $TRUST_CACERTS;
do
    BASE=$(basename $FILE)
    /opt/java/openjdk/bin/keytool -delete -alias $BASE
    /opt/java/openjdk/bin/keytool -import -trustcacerts -storepass changeit -file $FILE -cacerts -noprompt -alias $BASE
done

exec "$@"
