#!/usr/bin/env bash

version=$1
numPeers=$2

if [ $# -ne 3 ]; then
    echo "Usage: $0 <version> <number of peers>"
    exit 1
fi

for ((i = 1 ; i <= numPeers ; i++)); do
    gnome-terminal -x sh -c "java Peer $version $i hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082"
done