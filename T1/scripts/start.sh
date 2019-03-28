#!/usr/bin/env bash

version=$1
numPeers=$2

for ((i = 0 ; i < numPeers ; i++)); do
    gnome-terminal -e 'java Peer $version $i hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082'
done

gnome-terminal -e 'java TestApp hello STATE'