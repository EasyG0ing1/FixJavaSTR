#!/bin/bash

cur=$(pwd)
JP=$(dirname "$cur")
G="$JP/graalvm"
T="$JP/target"

java --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar $T/FixJavaSTR-jar-with-dependencies.jar

