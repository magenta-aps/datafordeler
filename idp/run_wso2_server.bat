@echo off

IF NOT DEFINED JAVA_HOME GOTO set_java_home
goto runserver

:set_java_home
set JAVA_HOME=c:\program files\java\jdk1.8.0_121
goto runserver


:runserver
start wso2\bin\wso2server.bat -run

:end
