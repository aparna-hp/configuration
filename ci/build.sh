#!/usr/bin/env bash

## Build script for ng-collection-service

APP_VERSION="1.0.0"

function usage()
{
    cat <<EOF
Usage: build.sh [options]
Builds collection-service
Options:
  -h, --help               Show this help message and exit.
  --jenkins                To be used when triggering builds from jenkins.
  --dev                    Local development builds to build the app and the docker image.
  --deploy                 Deploys the docker image to dockerhub after building.
  --build                  Builds repository using docker.
  --run_tests              Build and runs unit/integration tests.
  --build_docker_image     Builds repository using docker.
  --rmi                    Cleans up the docker image.
  --bes                    Triggering builds on IT executors
EOF
}

jenkins=false
dev=false
deploy=false
build=false
run_tests=false
build_docker_image=false
rmi=false
bes=false
while [ $# -gt 0 ]; do
    arg="$1"
    shift
    case "$arg" in
        -h|--help|help)
            usage
            exit 0;;
        --jenkins)
             jenkins=true
             ;;
        --dev)
            dev=true
            ;;
        --build)
            build=true
            ;;
        --run_tests)
            run_tests=true
            ;;
        --build_docker_image)
            build_docker_image=true
            ;;
        --deploy)
            deploy=true
            ;;
        --rmi)
            rmi=true
            ;;
        --bes)
            bes=true
            ;;
        *)
            error "Unknown option."
            usage
            exit 0;;
    esac
done


validateNotEmpty () {
    if [[ (${!1} == "") ]]; then
        echo variable $1 is empty, cancelling
        exit 1
    fi
}

# Check and set build image
if [ -z "${BUILD_IMAGE}" ]; then
    echo "BUILD_IMAGE is not set";
    BUILD_IMAGE=amazoncorretto:17.0.7-alpine
    echo "Setting BUILD_IMAGE to ${BUILD_IMAGE}"
else
    echo "BUILD_IMAGE is set to ${BUILD_IMAGE}"
fi

# Check and set base image
if [ -z "${BASE_IMAGE}" ]; then
    echo "BASE_IMAGE is not set";
    BASE_IMAGE=dockerhub.cisco.com/robot-dockerprod/cw-alpine-jre17-base:3.17.2-dc0916957.16
    echo "Setting BASE_IMAGE to ${BASE_IMAGE}"
else
    echo "BASE_IMAGE is set to ${BASE_IMAGE}"
fi

if [ ${bes} = true ]; then
    DOCKER_CMD="sudo docker "
else
    DOCKER_CMD="docker "
fi
${DOCKER_CMD} login -u robot-docker-v2-deployer -p 9mp37cxf88f7i4gn dockerhub.cisco.com

GRADLE_BUILD="gradlew clean build --refresh-dependencies --rerun-tasks"

if [ ${jenkins} = true ] || [ ${dev} = true ]; then


    if [ ${build} = true ]; then
        echo "Building collection service inside ${DOCKER_CMD}"
        # Create build container
        ${DOCKER_CMD} build --build-arg BUILD_IMG=${BUILD_IMAGE} --file ./ci/Dockerfile.build -t collection-service-build .

        # Run builds and unit tests
        ${DOCKER_CMD} run --rm -v $(pwd):/ws --name collection-service-builder collection-service-build ./${GRADLE_BUILD}

        if [[ $? -ne 0 ]]; then
            echo "Build failed!!!"
            exit 1
        fi

        # cleanup build image
        ${DOCKER_CMD} rmi collection-service-build
        if [[ $? -ne 0 ]]; then
            echo "Cleanup of build failed!!!"
            #exit 1
        fi
        exit 0
    fi

    if [ ${build_docker_image} = true ]; then
        echo "Starting docker image build"
        DOCKER_IMAGE_TMP_NAME="collection-service-tmp-tag:0.0"
        cd $(dirname ${0})/../
        echo "Building temporary docker image from `pwd`"
        ${DOCKER_CMD} build \
        --no-cache --rm \
        --build-arg BUILD_IMG=${BUILD_IMAGE} \
        --build-arg BASE_IMG=${BASE_IMAGE} \
        -f Dockerfile \
        -t ${DOCKER_IMAGE_TMP_NAME} .
        #${DOCKER_CMD} build --platform linux/amd64 -t ${DOCKER_IMAGE_TMP_NAME} .

        if [[ $? -ne 0 ]]; then
            echo "Docker build failed!!!"
            exit 1
        fi
        cd -
        echo "A docker image was created in the local repo with this tag: ["${DOCKER_IMAGE_TMP_NAME}"]"

        if [ ${jenkins} = true ]; then
            BRANCH_NAME=${GIT_BRANCH#*/}
        else
            BRANCH_NAME=`git branch --show-current`
        fi

        PRODUCT_PREFIX="wae"
        DOCKER_DIR="ng-collection-service"
        COMPONENT="ng-collection-service"
        VERSION=${APP_VERSION}
        DOCKER_SERVER=dockerhub.cisco.com
        HASH=`git rev-parse --short=9 HEAD`
        if [[ ($1 == "dev") ]]; then
            DATE=`date +%y%m%d%H%M`
        else
            DATE=`date +%y%m%d`
        fi

        if [ ${jenkins} = true ]; then
            validateNotEmpty BUILD_NUMBER
            TAG=${VERSION}-${BUILD_NUMBER}-${HASH}-${BRANCH_NAME}-${DATE}
        else
            TAG=${VERSION}-${HASH}-${BRANCH_NAME}-${DATE}
        fi

        validateNotEmpty COMPONENT
        validateNotEmpty BRANCH_NAME
        validateNotEmpty VERSION
        validateNotEmpty HASH
        validateNotEmpty DATE

        DOCKER_UPLOAD_IMAGE_NAME=${DOCKER_SERVER}/wae-docker/${DOCKER_DIR}/${PRODUCT_PREFIX}-${COMPONENT}:${TAG}

        # let's have some debug info on all of them
        echo "DOCKER_SERVER:            ["${DOCKER_SERVER}"]"
        echo "HASH:                     ["${HASH}"]"
        echo "DATE:                     ["${DATE}"]"
        echo "PRODUCT_PREFIX:           ["${PRODUCT_PREFIX}"]"
        echo "COMPONENT:                ["${COMPONENT}"]"
        echo "VERSION:                  ["${VERSION}"]"
        echo "BUILD_NUMBER:             ["${BUILD_NUMBER}"]"
        echo "GIT_BRANCH:               ["${BRANCH_NAME}"]"
        echo "DOCKER_UPLOAD_IMAGE_NAME: ["${DOCKER_UPLOAD_IMAGE_NAME}"]"


        echo "Re-tagging the docker image"
        ${DOCKER_CMD} tag ${DOCKER_IMAGE_TMP_NAME} ${DOCKER_UPLOAD_IMAGE_NAME}
        if [ ${deploy} = true ]; then
            echo "Pushing the docker image"
            #${DOCKER_CMD} login -u ${DOCKER_USER_NAME} -p ${DOCKER_PWD} ${DOCKER_SERVER}
            ${DOCKER_CMD} push ${DOCKER_UPLOAD_IMAGE_NAME}
            echo COLLECTION_SERVICE_IMAGE=${DOCKER_UPLOAD_IMAGE_NAME} > collection_image.properties
        else
            echo "Skipping pushing the docker image to ${DOCKER_SERVER}"
        fi

        echo "Cleaning up temporary image"
        # cleanup build image
        ${DOCKER_CMD} rmi ${DOCKER_IMAGE_TMP_NAME}
        if [[ $? -ne 0 ]]; then
            echo "Cleanup of docker images failed!!!"
        fi

        if [ ${rmi} = true ]; then
            echo "Removing docker image: ${DOCKER_UPLOAD_IMAGE_NAME}"
            ${DOCKER_CMD} rmi ${DOCKER_UPLOAD_IMAGE_NAME}
            if [[ $? -ne 0 ]]; then
                echo "Cleanup of docker image failed!!!"
            fi
        fi
    fi
else
    echo ""
    echo "Starting build"
    ./${GRADLE_BUILD}
    echo "Skipping docker image generation"
fi

