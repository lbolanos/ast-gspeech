#!/bin/sh
BASEDIR=$(dirname $0)
#echo $BASEDIR
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials-key.json
java -cp $BASEDIR/../target/astexample-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.astexample.TestRecognize