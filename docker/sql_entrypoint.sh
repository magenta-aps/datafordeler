#!/bin/bash
/app/create_db.sh &
exec "$@"
