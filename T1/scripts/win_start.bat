@echo off
set version=%1
set numPeers=%2
set start=%3

REM initiate peers
for /l %%x in (%start%, 1, %numPeers%) do (
    cd ../src
    start cmd.exe /k "java Peer %version% %%x hello 224.0.0.0 8001 224.0.0.1 8081 224.0.0.2 8082"
)
pause


