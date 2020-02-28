@echo off

set THISDIR=%~dp0%

set VERSION=0.0.1
set COREJAR=datafordeler-core-exec.jar

pushd %THISDIR%..\core
set COREDIR=%CD%
popd
