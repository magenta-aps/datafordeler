@echo off

REM set root dir
set PROJECT_ROOT_DIR=%~dp0%

REM load virtual env
call "%PROJECT_ROOT_DIR%python-env\Scripts\activate"

REM Configure python include path
IF NOT DEFINED PYTHONPATH GOTO empty_path
