@echo off

set GIT_EXE=c:\program files\git\bin\git.exe

rem Get current branch name
for /f %%i in ('"%GIT_EXE%" rev-parse --abbrev-ref HEAD') do set GIT_BRANCH=%%i

if not exist "%GIT_EXE%" (
    echo "Could not find git, can not run this script"
    goto end
)

"%GIT_EXE%" ls-tree -r --name-only "%GIT_BRANCH%" wso2\ > "%~dp0%unzip_exclude_list.txt"

echo List of excluded files updated successfully.
echo.
echo Now contains:
echo.
type "%~dp0%unzip_exclude_list.txt"
echo.

:end
pause