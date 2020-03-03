@echo off

set VERSION=0.0.1

set THISDIR=%~dp0%

set VERSION=0.0.1

pushd %THISDIR%..\..\core
set COREDIR=%CD%
popd
set COREJAR=datafordeler-core.jar

set PLUGINJAR=datafordeler-plugin-adresseservice-1.0-SNAPSHOT.jar
