@echo off

set DIR=%~dp0%
set RUN_ARGS=-Dlog4j2.formatMsgNoLookups=true -Dloader.path=../plugin/jar/
set RUN_JAR=%DIR%target\dafo-dataprovider.war


set DATABASE_CLASS=com.microsoft.sqlserver.jdbc.SQLServerDriver
set DATABASE_URL=jdbc:sqlserver://agint02listen:49700;databaseName=Datafordeler;IntegratedSecurity=true
set DATABASE_DIALECT=org.hibernate.spatial.dialect.sqlserver.SqlServer2008SpatialDialect
set DATABASE_SHOW_SQL=false
set DATABASE_USERNAME=
set DATABASE_PASSWORD=
set DATABASE_METHOD=update
set DATABASE_DEFAULT_SCHEMA=dbo

set SECONDARY_DATABASE_CLASS=com.microsoft.sqlserver.jdbc.SQLServerDriver
set SECONDARY_DATABASE_URL=jdbc:sqlserver://agint02listen:49700;databaseName=dafodb005;IntegratedSecurity=true
set SECONDARY_DATABASE_DIALECT=org.hibernate.spatial.dialect.sqlserver.SqlServer2008SpatialDialect
set SECONDARY_DATABASE_SHOW_SQL=false
set SECONDARY_DATABASE_USERNAME=
set SECONDARY_DATABASE_PASSWORD=
set SECONDARY_DATABASE_METHOD=update
set SECONDARY_DATABASE_DEFAULT_SCHEMA=dbo


rem Load global settings
call "%DIR%settings.bat"

rem Add local settings, if present
if exist "%DIR%local_settings.bat" (
    call "%DIR%local_settings.bat"
)

rem If a local_settings.properties file exists, make sure it's loaded after the application.properties
if exist "%DIR%local_settings.properties" (
    set RUN_ARGS=%RUN_ARGS% -Dspring.config.location="classpath:/application.properties,file:%DIR%local_settings.properties"
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

echo "Build geo"
pushd %DIR%..\plugin\geo
    call mvnw.cmd -DskipTests clean install
popd

echo "Build cvr"
pushd %DIR%..\plugin\cvr
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

echo "Build eskat"
pushd %DIR%..\plugin\eskat
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
call "%JAVA_HOME%\bin\java.exe" -cp "%RUN_JAR%" %RUN_ARGS% org.springframework.boot.loader.PropertiesLauncher
