@echo off
set "cur=%cd%"
for %%i in ("%cur%\..") do set "JP=%%~fi"
set "G=%JP%\graalvm"
set "T=%JP%\target"

java --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "%T%\FixJavaSTR-jar-with-dependencies.jar"
