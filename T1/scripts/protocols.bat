@echo off

set delete="DELETE"
set argCount=0

for %%x in (%*) do set /A argCount+=1

IF %argCount%==1 IF "%1"=="true" (
    set delete="DELETEENH"
) ELSE (
    IF %argCount%==1 (
        echo Usage: %0 [enhancement=true]
        exit /B 1  
    )
)

start cmd.exe /k "java TestApp hello STATE"
start cmd.exe /k "java TestApp hello RECLAIM 0"
start cmd.exe /k "java TestApp hello %delete% %userprofile%\Desktop\resumosPPIN1.pdf"
pause