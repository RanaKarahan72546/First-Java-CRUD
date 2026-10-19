@echo off
cd /d "%~dp0"
if not exist out mkdir out
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d out *.java
if errorlevel 1 (
    echo Compilation failed. Install JDK 17 or newer and check java -version.
    pause
    exit /b 1
)
java -cp out course.crud.Application
pause
