@echo off
set "cur=%cd%"
for %%i in ("%cur%\..") do set "JP=%%~fi"
cd /d "%JP%"
call mvn -f pom.xml clean package
cd /d "%cur%"
