@echo off
set PYTHON_EXE=python.exe

rem Check for python executeable
where /q %PYTHON_EXE% && GOTO pythonok

if exist "c:\Python27\python.exe" (
    SET PYTHON_EXE=c:\Python27\python.exe
    echo Existing python.exe found
) else (
    GOTO nopython
)

:pythonok
echo Running initial_setup.py
%PYTHON_EXE% "%~dp0%bin\initial_setup.py"

rem Everything should be good if we get here
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