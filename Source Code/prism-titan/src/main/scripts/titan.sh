#!/bin/bash
#
#
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
[ -z "$PROCESSOR_HOME" ] && PROCESSOR_HOME=`cd "$PRGDIR/.." ; pwd`

SERVER_NAME=gmd-client

# path
BIN_PATH=$PROCESSOR_HOME/bin
LOG_PATH=$PROCESSOR_HOME/logs
LIB_PATH=$PROCESSOR_HOME/lib
#
mkdir -p $LOG_PATH
touch $LOG_PATH/stdout.log

#
CLASS_NAME=edu.prism.TitanApp
CLASS_PATH=$PROCESSOR_HOME/conf
#
for f in $LIB_PATH/*.jar
do
    CLASS_PATH=$CLASS_PATH:$f;
done

DEBUG_ARGS="";
PROGRAM_ARGS="-Xms1g -Xmx1g -Dapp.name=${SERVER_NAME} -Dapp.base=${PROCESSOR_HOME} -XX:+UseConcMarkSweepGC -server -XX:SurvivorRatio=5 -XX:CMSInitiatingOccupancyFraction=80 -XX:+PrintTenuringDistribution  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime ${DEBUG_ARGS} -Xloggc:./gc.log"
java $PROGRAM_ARGS -classpath $CLASS_PATH $CLASS_NAME $@