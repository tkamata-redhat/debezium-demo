#!/bin/bash

SCRIPT_PATH=$(dirname "$0")

if [[ "$CONNECT_HOST" == "" ]]; then
  CONNECT_HOST=localhost:8083
fi

for i in ${SCRIPT_PATH}/setup/*.json; do
  echo "Registering connector by $i"
  curl -X POST "http://${CONNECT_HOST}/connectors" \
   -H "Content-Type: application/json" \
   --data "@$i" -v
  echo
done
