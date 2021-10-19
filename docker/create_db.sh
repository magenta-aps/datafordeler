#!/bin/bash
#run the setup script to create the DB and the schema in the DB
#do this in a loop because the timing for when the SQL instance is ready is indeterminate
for i in {1..50};
do
    /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P testTEST1 -d master -i createdbs.sql
    if [ $? -eq 0 ]
    then
        echo "createdbs.sql completed"
        break
    else
        echo "not ready yet..."
        sleep 1
    fi
done

sleep 20
#Just a simple wait 20 seconds before initiating the database with basic values
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P testTEST1 -d master -i initiate-db.sql
