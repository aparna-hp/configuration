#!/bin/bash
set -e

#
# Java options
#
JVM_OPTS="-Dlogging.config=$CONFIG_HOME/logback.xml"

# Enable for explicitly passing config files and directories
#JVM_OPTS="-Dlogging.config=/config/logback.xml -Dspring.config.name=application,engine,topology,actions -Dspring.config.location=$CONFIG_HOME"

# Enable for remote debugging
#JVM_OPTS="$JVM_OPTS -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n"

# Enable for JMX/jconsole remote connection
#JVM_OPTS="$JVM_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

export JVM_OPTS

echo "Loading configs from: ${SPRING_CONFIG_LOCATION}"
echo "Config list: ${SPRING_CONFIG_NAME}"

# Check and set datasource creds
if [ -z "${SPRING_DATASOURCE_PASSWORD}" ]; then
    echo "Datasource creds not set";
    jdbcCreds=($(/health/brew LATTE@CWORK-SCRT-CAFE-3.0-2018 $(env | grep UUID | cut -d "=" -f2) POSTGRES))
    export SPRING_DATASOURCE_USERNAME=${jdbcCreds[0]}
    export SPRING_DATASOURCE_PASSWORD=${jdbcCreds[1]}
else
    echo "Using datasource creds from env"
fi
# Export log dir
export LOG_HOME=/var/log/robot/

# TODO - remove this line
echo "brew: ${SPRING_DATASOURCE_USERNAME} ${SPRING_DATASOURCE_PASSWORD}"

# Run application
echo "Running ${BIN}"
cd $ROBOT_BIN
exec java $JVM_OPTS -jar $BIN