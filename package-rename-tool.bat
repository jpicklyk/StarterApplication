batch
@echo off
setlocal enabledelayedexpansion

:: Prompt for the current and new package names
::set /p currentPackageName="Enter the current package name (e.g., com.example.app): "
set currentPackageName="com.example.starterapplication"
set /p newPackageName="Enter the new package name (e.g., com.mynewapp): "

:: Replace dots with backslashes for directory paths
set currentPackagePath=%currentPackageName:.=\%

set newPackagePath=%newPackageName:.=\%

:: Recursively find and rename directories
for /r %%d in (%currentPackagePath%) do (
    set dirName=%%~nxd
    set newDir=%%~dpd%newPackagePath%\!dirName!
    if not exist "!newDir!" (
        ren "%%d" "!dirName!"
        move "%%~dpd\!dirName!" "!newDir!"
    )
)

:: Recursively find and modify files
for /r %%f in (*.java *.kt *.xml *.gradle *.properties) do (
    set filePath=%%f
    call :replaceInFile "!filePath!" "%currentPackageName%" "%newPackageName%"
)

echo Package name changed successfully!
pause
exit /b

:: Function to replace text in a file
:replaceInFile
set "file=%~1"
set "oldText=%~2"
set "newText=%~3"

for /f "tokens=1,* delims=¶" %%A in ('"type %file% | find /v /n "" "') do (
    set "line=%%B"
    if defined line (
        set "line=!line:%oldText%=%newText%!"
        echo(!line!>> "%file%.temp"
    ) else (
        echo.>> "%file%.temp"
    )
)

move /y "%file%.temp" "%file%" >nul
exit /b