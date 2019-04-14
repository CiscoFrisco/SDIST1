#!/usr/bin/env bash

delete="DELETE"

if [ $1 = "true" ]; then
    delete="DELETEENH"
else
    echo "Usage: $0 [enhancement=true]"
fi


java TestApp hello STATE
java TestApp hello RECLAIM 0
java TestApp hello $delete ~/Desktop/resumosPPIN1.pdf