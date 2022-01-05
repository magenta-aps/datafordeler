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

echo "Build parent"
pushd %DIR%..\plugin\parent
   call mvnw.cmd -DskipTests clean install
popd

echo "Build core"
pushd %COREDIR%
   call mvnw.cmd -DskipTests clean install
popd

echo "Build cpr"
pushd %DIR%..\plugin\cpr
    call mvnw.cmd -DskipTests clean install
popd

echo "Build cvr"
pushd %DIR%..\plugin\cvr
    call mvnw.cmd -DskipTests clean install
popd

echo "Build geo"
pushd %DIR%..\plugin\geo
    call mvnw.cmd -DskipTests clean install
popd

echo "Build ger"
pushd %DIR%..\plugin\ger
    call mvnw.cmd -DskipTests clean install
popd

echo "Build subscription"
pushd %DIR%..\plugin\subscription
    call mvnw.cmd -DskipTests clean install
popd

echo "Build eboks"
pushd %DIR%..\plugin\eboks
    call mvnw.cmd -DskipTests clean install
popd

echo "Build prisme"
pushd %DIR%..\plugin\prisme
    call mvnw.cmd -DskipTests clean install
popd

echo "Build combinedPitu"
pushd %DIR%..\plugin\combinedPitu
    call mvnw.cmd -DskipTests clean install
popd

echo "Build statistik"
pushd %DIR%..\plugin\statistik
    call mvnw.cmd -DskipTests clean install
popd

rem Copy compiled WAR so running will not hold a lock on the compiled file destination
copy "%DIR%\target\%COREJAR%" "%RUN_JAR%"

rem Run the JAR file
call "%JAVA_HOME%\bin\java.exe" -jar "%RUN_JAR%" %RUN_ARGS%
