@echo off
setlocal

if "%~1"=="" (
    echo Usage: %~nx0 ^<java_project_directory>^>
    exit /b 1
)

set "TARGET_DIR=%~1"
set "CUR_DIR=%CD%"
for %%I in ("%CUR_DIR%") do set "JP=%%~dpI"
set "JP=%JP:~0,-1%"
set "G=%JP%\src\main\resources\META-INF\native-image\com.simtechdata\FixJavaSTR"
set "T=%JP%\target"

call mvn -f "%JP%\pom.xml" clean package

rmdir /S /Q "%JP%\src\main\resources\META-INF"
del "%TARGET_DIR%\JavaFiles.zip"

java -agentlib:native-image-agent=config-merge-dir="%G%" --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "%T%\FixJavaSTR-jar-with-dependencies.jar" unzip "%TARGET_DIR%\JavaFiles.zip"
java -agentlib:native-image-agent=config-merge-dir="%G%" --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "%T%\FixJavaSTR-jar-with-dependencies.jar" show "%TARGET_DIR%"
java -agentlib:native-image-agent=config-merge-dir="%G%" --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "%T%\FixJavaSTR-jar-with-dependencies.jar" fix "%TARGET_DIR%" JavaFiles.zip
java -agentlib:native-image-agent=config-merge-dir="%G%" --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "%T%\FixJavaSTR-jar-with-dependencies.jar" version

endlocal
