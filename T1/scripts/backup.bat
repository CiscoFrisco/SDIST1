@echo off

set backup="BACKUP"
set argCount=0

for %%x in (%*) do set /A argCount+=1

IF %1==true IF NOT %argCount%==1 (
    echo Usage: %0 [enhancement=true]
    exit /B 1  
) ELSE IF %argCount%==1  (
    set restore="BACKUPENH"
)

start cmd.exe /k "java TestApp hello %backup% %userprofile%\Desktop\resumosPPIN1.pdf 2"
start cmd.exe /k "java TestApp hello %backup% %userprofile%\Desktop\iron_man.jpg 2"
start cmd.exe /k "java TestApp hello %backup% %userprofile%\Desktop\a6.pdf 2"
start cmd.exe /k "java TestApp hello %backup% %userprofile%\Desktop\C\ book\ 1.pdf 2"
pause

