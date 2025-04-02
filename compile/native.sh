#!/bin/bash

cur=$(pwd)
JP=$(dirname "$cur")
T="$JP/target"
S="$cur/bin"

mvn -f $JP/pom.xml clean -Pnative native:compile
