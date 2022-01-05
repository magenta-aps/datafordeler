@echo off

set DIR=%~dp0%
set RUN_ARGS="-Dlog4j2.formatMsgNoLookups=true"
set RUN_JAR=%DIR%target\dafo-dataprovider.war

rem Load global settings
call "%DIR%settings.bat"

rem Add local settings, if present
if exist "%DIR%local_settings.bat" (
    call "%DIR%local_settings.bat"
)

rem If a local_settings.properties file exists, make sure it's loaded after the application.properties
if exist "%DIR%local_settings.properties" (
    set RUN_ARGS=%RUN_ARGS% --spring.config.location="classpath:/application.properties,file:%DIR%local_settings.properties"
)

echo "Build core"
pushd %COREDIR%
   call mvnw.cmd clean install
popd

echo "Build cpr"
pushd %DIR%..\plugin\cpr
    call mvnw.cmd clean install
popd

echo "Build cvr"
pushd %DIR%..\plugin\cvr
    call mvnw.cmd clean install
popd

echo "Build gladdrreg"
pushd %DIR%..\plugin\gladdrreg
    call mvnw.cmd clean install
popd

rem Copy compiled WAR so running will not hold a lock on the compiled file destination
copy "%DIR%\target\%COREJAR%" "%RUN_JAR%"

rem Run the JAR file
call "%JAVA_HOME%\bin\java.exe" -jar "%RUN_JAR%" %RUN_ARGS%
