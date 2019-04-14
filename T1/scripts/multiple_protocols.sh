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


gnome-terminal -x sh -c "java TestApp hello STATE; sleep 5"
gnome-terminal -x sh -c "java TestApp hello RECLAIM 0; sleep 5"
gnome-terminal -x sh -c "java TestApp hello $delete ~/Desktop/resumosPPIN1.pdf; sleep 5"