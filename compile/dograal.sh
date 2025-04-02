#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <java_project_directory>"
  exit 1
fi

TARGET_DIR=$1
cur=$(pwd)
JP=$(dirname "$cur")
G="$JP/src/main/resources/META-INF/native-image/com.simtechdata/FixJavaSTR"
T="$JP/target"

mvn -f $JP/pom.xml clean package
rm -R $JP/src/main/resources/META-INF
rm "$TARGET_DIR/JavaFiles.zip"
java -agentlib:native-image-agent=config-merge-dir=$G --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "$T/FixJavaSTR-jar-with-dependencies.jar" show "$TARGET_DIR"
java -agentlib:native-image-agent=config-merge-dir=$G --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "$T/FixJavaSTR-jar-with-dependencies.jar" fix "$TARGET_DIR" JavaFiles.zip
java -agentlib:native-image-agent=config-merge-dir=$G --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "$T/FixJavaSTR-jar-with-dependencies.jar" unzip "$TARGET_DIR/JavaFiles.zip"
java -agentlib:native-image-agent=config-merge-dir=$G --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar "$T/FixJavaSTR-jar-with-dependencies.jar" version
