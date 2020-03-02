@echo off

set DIR=%~dp0%
set RUN_ARGS=""

rem Load global settings
call "%DIR%settings.bat"

rem Add local settings, if present
if exist "%DIR%local_settings.bat" (
    call "%DIR%local_settings.bat"
)

rem If the core JAR file does not exist, build it
if not exist "%COREDIR%\target\%COREJAR%" (
    echo %COREDIR%\target\%COREJAR% not found, building project
	pushd %COREDIR%
    call mvnw.cmd -Dmaven.test.skip=true package
	popd
)

call mvnw.cmd install:install-file -Dfile="%COREDIR%\target\%COREJAR%" -DgroupId=dk.magenta.datafordeler -DartifactId=datafordeler-core -Dversion=1.0-SNAPSHOT -Dpackaging=jar

rem Build the jar
pushd %DIR%
call mvnw.cmd clean install
popd