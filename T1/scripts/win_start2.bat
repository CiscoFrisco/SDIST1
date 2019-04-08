@echo off
set version= %1
set numPeers= %2
set argCount=0

REM count number of arguments 
REM for %%x in (%*) do (
REM    set argCount += 1
REM )

REM echo %argCount% <- está sempre a 0. o ciclo de cima não está bem e por isso não dá para fazer a verificação de baixo

REM check number of arguments
REM if  %argCount% NEQ 3 (
REM     echo "Usage: $0 <version> <number of peers>"
REM     exit /B 1
REM )

REM initiate peers
for /l %%x in (%numPeers%, -1, 1) do (
    cd ../src
    start cmd.exe /k "java Peer %version% %%x hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082"
)
pause


