#!/bin/sh

java -version

TRUST_CACERTS=${TRUST_CACERTS:=""}
IFS=,

# Import TRUST_CACERTS to trust-store
for FILE in $TRUST_CACERTS;
do
    BASE=$(basename $FILE)
    /usr/bin/keytool -delete -alias $BASE
    /usr/bin/keytool -import -trustcacerts -storepass changeit -file $FILE -cacerts -noprompt -alias $BASE
done

exec "$@"
