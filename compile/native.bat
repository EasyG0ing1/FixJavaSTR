@echo off
set "cur=%cd%"
for %%i in ("%cur%\..") do set "JP=%%~fi"
set "T=%JP%\target"
set "S=%cur%\bin"
cd /d "%JP%"
call mvn -f pom.xml clean -Pnative native:compile
cd /d "%cur%"
