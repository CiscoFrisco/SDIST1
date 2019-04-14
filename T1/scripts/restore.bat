@echo off

set restore="RESTORE"
set argCount=0

for %%x in (%*) do set /A argCount+=1

IF %argCount%==1 IF "%1"=="true" (
    set restore="RESTOREENH"
) ELSE (
    IF %argCount%==1 (
        echo Usage: %0 [enhancement=true]
        exit /B 1  
    )
)

start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\resumosPPIN1.pdf"
start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\iron_man.jpg"
start cmd.exe /k "java TestApp hello %restore% %userprofile%\Desktop\a6.pdf"
pause

