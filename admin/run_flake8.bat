@echo off
call "%~dp0%configure_environment.bat"
flake8 --exclude="migrations,data" django/dafousers/ django/dafoconfig/ django/dafoadmin_site/
pause
