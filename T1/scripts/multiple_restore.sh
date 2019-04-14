#!/usr/bin/env bash

restore="RESTORE"

if [ $1 = "true" ]; then
    restore="RESTOREENH"
else
    echo "Usage: $0 [enhancement=true]"
fi

java TestApp hello $restore ~/Desktop/resumosPPIN1.pdf
java TestApp hello $restore ~/Desktop/iron_man.jpg
java TestApp hello $restore ~/Desktop/a6.pdf
java TestApp hello $restore ~/Desktop/C\ book\ 1.pdf
