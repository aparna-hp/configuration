#!/bin/bash

OK=$(netstat -tulpn | grep LISTEN | grep 8080 | wc -l)

if [ "$OK" == "1" ]; then
	/health/update_health.sh 0
	exit 0
else
	/health/update_health.sh 1 "Application not listening on port 8080 yet"
	exit 1
fi