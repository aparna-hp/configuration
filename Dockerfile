##
## Builds docker image for design-visualization-service
##

ARG BUILD_IMG
ARG BASE_IMG
FROM --platform=linux/amd64 ${BUILD_IMG} AS build

ENV http_proxy=http://proxy-wsa.esl.cisco.com:80
ENV https_proxy=http://proxy-wsa.esl.cisco.com:80
ENV ftp_proxy=http://proxy-wsa.esl.cisco.com:80
ENV no_proxy="localhost,.cisco.com,172.29.104.,172.29.105.,172.28.186.,10.,2001:420:28f:2010::/60"

#RUN apk update && apk add --no-cache gradle
WORKDIR /
COPY . ./
COPY gradlew ./
COPY gradle.properties ./
COPY settings.gradle ./

#Build
RUN ./gradlew clean build --refresh-dependencies --rerun-tasks


###
### Deploy
###
FROM --platform=linux/amd64 ${BASE_IMG}

LABEL maintainer="aparp@cisco.com" \
		name="cp-collection-service" \
        description="Docker image for CW planning collection service" \
        version="1.0.0" \
        base_image=${BASE_IMG}

# supervisor variables
# PROGRAM1 should match serviceName in manifest
ENV SUPERVISOR_PROGRAM1 collection-service
ENV SUPERVISOR_COMMAND1 "$ROBOT_BIN/start.sh"
ENV SUPERVISOR_STOPASGROUP1 "true"
ENV SUPERVISOR_KILLASGROUP1 "true"


ENV BIN "ConfigService-1.0.0-SNAPSHOT.jar"
ENV CONFIG_HOME "/etc/robot/config/"
ENV ROBOT_BIN "/opt/robot/bin"
WORKDIR /

#Copy build artifact and scripts
COPY --from=build /build/libs/$BIN $ROBOT_BIN/

COPY docker/resources/start.sh $ROBOT_BIN
RUN chmod -R +x $ROBOT_BIN/start.sh
COPY docker/resources/restart.sh $ROBOT_BIN
RUN chmod -R +x $ROBOT_BIN/restart.sh

RUN mkdir -p /config/

COPY docker/resources/start.sh /
RUN chmod -R +x /start.sh

# Copy config files
RUN mkdir -p $CONFIG_HOME
COPY src/main/resources/application.properties $CONFIG_HOME/
COPY src/main/resources/logback.xml $CONFIG_HOME/

ENV SPRING_CONFIG_NAME "application"
ENV SPRING_CONFIG_LOCATION "file://${CONFIG_HOME}"

# Copy showtech script
RUN mkdir -p $ROBOT_BIN/showtech
COPY docker/resources/showtech.sh $ROBOT_BIN/showtech
RUN chmod -R +x $ROBOT_BIN/showtech/showtech.sh

# Copy health and readiness scripts
RUN mkdir -p $ROBOT_BIN/health
COPY docker/resources/readiness.sh $ROBOT_BIN/health
RUN chmod -R +x $ROBOT_BIN/health/readiness.sh

EXPOSE 8080
