@echo off

REM set root dir
set DJANGOPROJECT_ROOT_DIR=%~dp0%

REM load virtual env
call "%DJANGOPROJECT_ROOT_DIR%python-env\Scripts\activate"

REM Configure python include path
IF NOT DEFINED PYTHONPATH GOTO empty_path

REM if path is set append to it
set PYTHONPATH=%PYTHONPATH%;%~dp0%django
GOTO finish

:empty_path
REM when path is not set, just set it
SET PYTHONPATH=%~dp0%django
GOTO finish

:finish
REM tell django which file to use for settings
SET DJANGO_SETTINGS_MODULE=dafoadmin_site.settings
