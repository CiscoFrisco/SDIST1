#!/usr/bin/env bash

restore="RESTORE"

if [ $# = 1 ] && [ $1 != "true" ] 
then
    echo "Usage: $0 [enhancement=true]"
    exit 1
elif [ $# = 1 ]
then
    restore="RESTOREENH"
fi

gnome-terminal -x sh -c "java TestApp hello $restore ~/Desktop/resumosPPIN1.pdf; sleep 5"
gnome-terminal -x sh -c "java TestApp hello $restore ~/Desktop/iron_man.jpg; sleep 5"
gnome-terminal -x sh -c "java TestApp hello $restore ~/Desktop/C\ book\ 1.pdf; sleep 5"
