@echo off

java -version:1.7 -version > nul 2>&1
if %ERRORLEVEL% == 0 goto found
echo [laFab] ERROR : laFabrique needs at least a JDK version above 1.7 to be installed.
goto end

:found
java {librairieList} {appClass} %*
:end


