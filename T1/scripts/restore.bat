@echo off

set restore="RESTORE"
set argCount=0

for %%x in (%*) do set /A argCount+=1

IF %1==true IF NOT %argCount%==1 (
    echo Usage: %0 [enhancement=true]
    exit /B 1  
) ELSE IF %argCount%==1  (
    set restore="RESTOREENH"
)

start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\resumosPPIN1.pdf 2"
start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\iron_man.jpg 2"
start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\a6.pdf 2"
start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\C\ book\ 1.pdf 2"
pause

