#!/bin/bash
set -e

echo "Restarting..."
supervisorctl reload &
echo "Done"