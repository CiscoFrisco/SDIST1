#!/usr/bin/env bash

backup="BACKUP"

if [ $1 = "true" ]; then
    backup="BACKUPENH"
else
    echo "Usage: $0 [enhancement=true]"
fi

java TestApp hello $backup ~/Desktop/resumosPPIN1.pdf 2
java TestApp hello $backup ~/Desktop/iron_man.jpg 2
java TestApp hello $backup ~/Desktop/a6.pdf 2
java TestApp hello $backup ~/Desktop/C\ book\ 1.pdf 2
