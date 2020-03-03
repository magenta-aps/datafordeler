@echo off
set VIRTUALENV_LOCATION=%~dp0%python-env

set PYTHON_EXE=python.exe
set PIP_EXE=pip.exe
set VIRTUALENV_EXE=virtualenv.exe

rem Check for python executeable
where /q %PYTHON_EXE% && GOTO pythonok

if exist "c:\Python27\python.exe" (
    SET PYTHON_EXE=c:\Python27\python.exe
) else (
    GOTO nopython
)

:pythonok
rem where /q %PIP_EXE%
rem if %ERRORLEVEL%==0 GOTO setup

if exist c:\Python27\Scripts\pip.exe (
    SET PIP_EXE=c:\Python27\Scripts\pip.exe
) else (
    GOTO nopip
)

:setup
rem if virtualenv folder is already installed we have no more to do
if exist "%VIRTUALENV_LOCATION%" GOTO activatevirtualenv

where /q %VIRTUALENV_EXE%
if %ERRORLEVEL%==0 GOTO setupvirtualenv

rem install virtualenv
%PIP_EXE% install virtualenv
where /q %VIRTUALENV_EXE%

rem if virtualenv is on the path just run it
if %ERRORLEVEL%==0 GOTO setupvirtualenv

rem If virtualenv is not found on the PATH, it should be in same
rem location as pip.exe
for %%F in (%PIP_EXE%) do set VIRTUALENV_DIR=%%~dpF
set VIRTUALENV_EXE=%VIRTUALENV_DIR%virtualenv.exe
GOTO setupvirtualenv

:setupvirtualenv
%VIRTUALENV_EXE% "%VIRTUALENV_LOCATION%"
GOTO activatevirtualenv

:activatevirtualenv
call "%~dp0%configure_environment.bat"
pip install -r "%~dp0%doc\requirements.txt"
GOTO end

:nopython
echo.
echo Could not find python.exe. Either make sure it is available in the PATH
echo or install python in c:\Python27.
echo.
echo Python can be downloaded from the following location:
echo.
echo   https://www.python.org/downloads/release/python-2710/
echo.
GOTO end

:nopip
echo.
echo Could not find pip.exe. Either make sure it is available in the PATH
echo or install python in c:\Python27.
echo.
echo Python can be downloaded from the following location:
echo.
echo   https://www.python.org/downloads/release/python-2710/
echo.
GOTO end

:end
echo.
echo Setup done.
echo.
pause