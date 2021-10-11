#!/bin/bash
#run the setup script to create the DB and the schema in the DB
#do this in a loop because the timing for when the SQL instance is ready is indeterminate
for i in {1..50};
do
    /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P testTEST1 -d master -i createdbs.sql
    if [ $? -eq 0 ]
    then
        echo "setup.sql completed"
        sleep 100
        echo "setup.sql WAITED"
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P testTEST1 -d master -i initiate-db.sql
        break
    else
        echo "not ready yet..."
        sleep 1
    fi
done
