#!/usr/bin/env bash

delete="DELETE"

if [ $# = 1 ] && [ $1 != "true" ] 
then
    echo "Usage: $0 [enhancement=true]"
    exit 1
elif [ $# = 1 ]
then
    delete="DELETEENH"
fi


java TestApp hello STATE
java TestApp hello RECLAIM 0
java TestApp hello $delete ~/Desktop/resumosPPIN1.pdf