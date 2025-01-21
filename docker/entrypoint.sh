#!/bin/bash

java -version

TRUST_CACERTS=${TRUST_CACERTS:=""}

# Import TRUST_CACERTS to trust-store
for FILE in ${TRUST_CACERTS//,/ }
do
    BASE="${FILE##*/}"
    BASE="${BASE%.*}"
    /usr/local/openjdk-23/bin/keytool -import -trustcacerts -storepass changeit -file $FILE -cacerts -noprompt -alias $BASE
done

exec "$@"
