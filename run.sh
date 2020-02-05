#!/usr/bin/env bash
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)

if [[ "$JAVA_VER" -lt "11" ]]
then
    echo "Sorry but JAVA 11 is required"
else
    mvn clean install -Dmaven.test.skip
    java -jar payments-processing-app/target/payments-processing-app-0.0.1-SNAPSHOT.jar
fi