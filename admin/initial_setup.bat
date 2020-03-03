@echo off
set VIRTUALENV_LOCATION=%~dp0%python-env

set PYTHON_EXE=python.exe
set PIP_EXE=pip.exe
set VIRTUALENV_EXE=virtualenv.exe
set MAMAGE_SCRIPT=%~dp0%django\manage.py

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
python "%~dp0%bin\pre_python_requirements.py"
pip install pypiwin32==219
pip install -r "%~dp0%doc\requirements.txt"
python "%~dp0%bin\initial_setup.py"
python "%MAMAGE_SCRIPT%" migrate
python "%~dp0%bin\check_superuser_exists.py"
if not %ERRORLEVEL%==0 (
    echo.
    echo Creating a new superuser for the admin system.
    echo Please fill out the required information.
    echo.
    python "%MAMAGE_SCRIPT%" createsuperuser
)
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