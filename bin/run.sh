#!/bin/sh
BASEDIR=$(dirname $0)
#echo $BASEDIR
export GOOGLE_APPLICATION_CREDENTIALS=$BASEDIR/APIProject-uyiuu8yuhh.json
java -Dlog4j.configuration=file:$BASEDIR/../resources/log4j.properties -cp $BASEDIR/../target/astgspeech-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.astgspeech.core.DefaultEAgiServer