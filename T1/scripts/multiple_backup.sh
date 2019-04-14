#!/usr/bin/env bash

backup="BACKUP"

if [ $# = 1 ] && [ $1 != "true" ] 
then
    echo "Usage: $0 [enhancement=true]"
    exit 1
elif [ $# = 1 ]
then
    backup="BACKUPENH"
fi

gnome-terminal -x sh -c "java TestApp hello $backup ~/Desktop/resumosPPIN1.pdf 2"
gnome-terminal -x sh -c "java TestApp hello $backup ~/Desktop/iron_man.jpg 2"
gnome-terminal -x sh -c "java TestApp hello $backup ~/Desktop/a6.pdf 2"
gnome-terminal -x sh -c "java TestApp hello $backup ~/Desktop/C\ book\ 1.pdf 2"
