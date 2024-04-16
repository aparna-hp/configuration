#!/bin/bash

# Print the service name
echo "***** SERVICE: $SERVICE_NAME *****"

# send SIGUSR2 to trigger showtech handler
crossworkctl signal java
echo "showtech collection for collection service complete!!!"