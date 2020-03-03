@echo off
call "%~dp0%configure_environment.bat"
start python "%DJANGOPROJECT_ROOT_DIR%django\manage.py" runserver 0.0.0.0:8000
