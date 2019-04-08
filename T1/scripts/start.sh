#!/usr/bin/env bash

version=$1
numPeers=$2
invert=$3

if [ $# -ne 3 ]; then
    echo "Usage: $0 <version> <number of peers> <invert>"
    exit 1
fi

if [ $invert = "true" ]; then
    for ((i = numPeers ; i > 0 ; i--)); do
        gnome-terminal -x sh -c "java Peer $version $i hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082"
    done
else
    for ((i = 1 ; i <= numPeers ; i++)); do
        gnome-terminal -x sh -c "java Peer $version $i hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082"
    done
fi