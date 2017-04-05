@echo off
rem
rem DO NOT ADD LOCAL ENVIRONMENT CONFIGURATION TO THIS FILE!
rem Instead create a file called "set_wso2_settings_local.bat" in the same
rem directory and override the set variables within that.
rem

rem
rem To add new configuration items to this file, add a variable in the top
rem section, use it in a commend line argument in the middle section and
rem make sure to unset it in the ending section. Also add the variable to the
rem set_wso2_settings_local-example.bat file.
rem


set DATABASE_URL="jdbc:sqlserver://127.0.0.1:1433;databaseName=dafo_wso2"
set DATABASE_USERNAME=dafo_wso2
set DATABASE_PASSWORD=dafo_wso2

set USERSTORE_URL="jdbc:sqlserver://127.0.0.1:1433;databaseName=dafo_users"
set USERSTORE_USERNAME=dafo_users
set USERSTORE_PASSWORD=dafo_users

rem Allow defaults to be overwritting by local script
if exist "%~dp0%set_wso2_settings_local.bat" (
    call "%~dp0%set_wso2_settings_local.bat"
)


rem Build command line args from the set variables
set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.database.url=%DATABASE_URL%
set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.database.username=%DATABASE_USERNAME%
set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.database.password=%DATABASE_PASSWORD%

set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.userstore.url=%USERSTORE_URL%
set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.userstore.username=%USERSTORE_USERNAME%
set DAFO_CMD_ARGS=%DAFO_CMD_ARGS% -Ddafo.userstore.password=%USERSTORE_PASSWORD%

rem Unset variables so we do not leak them
set DATABASE_URL=
set DATABASE_USERNAME=
set DATABASE_PASSWORD=

set USERSTORE_URL=
set USERSTORE_USERNAME=
set USERSTORE_PASSWORD=
