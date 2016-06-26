#!/bin/sh
BASEDIR=$(dirname $0)
#echo $BASEDIR
export GOOGLE_APPLICATION_CREDENTIALS=$BASEDIR/../../APIProject-ffad4a6777ad.json
java -Dlog4j.configuration=file:$BASEDIR/../resources/log4j.properties -cp $BASEDIR/../target/astgspeech-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.asteriskjava.fastagi.DefaultAgiServer