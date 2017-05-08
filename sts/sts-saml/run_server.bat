@echo off

set DIR=%~dp0%
set RUN_ARGS=

rem Load global settings
call "%~dp0%settings.bat"

rem Add local settings, if present
if exist "%DIR%local_settings.bat" (
    call "%DIR%local_settings.bat"
)

rem If a local_settings.properties file exists, make sure it's loaded after the application.properties
if exist "%DIR%local_settings.properties" (
    set RUN_ARGS=%RUN_ARGS% --spring.config.location="classpath:/application.properties,file:%DIR%local_settings.properties"
)

rem If the WAR file does not exist, build it
if not exist "%DIR%target\%WARNAME%" (
    echo %DIR%target\%WARNAME% not found, building project

    rem Build and install sts-library dependency
    cd "%DIR%..\sts-library"
    call "%DIR%..\sts-library\mvnw.cmd" -Dmaven.test.skip=true clean install

    rem Build the war
    cd %DIR%
    call "%DIR%mvnw.cmd" -Dmaven.test.skip=true package
)

rem Run the WAR file
call "%JAVA_HOME%\bin\java.exe" -jar "%DIR%target\%WARNAME%" %RUN_ARGS%
