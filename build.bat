@echo off
REM Java Project Build Script for Windows
REM Uses JDK 25

setlocal enabledelayedexpansion

REM Detect JAVA_HOME - use environment variable or try to find Java
if "%JAVA_HOME%"=="" (
    echo JAVA_HOME not set. Attempting to find Java...
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set "JAVA_PATH=%%i"
        goto :found_java
    )
    echo Error: Java not found. Please set JAVA_HOME or add Java to PATH.
    exit /b 1
    :found_java
    echo Found Java at: !JAVA_PATH!
    set "JAVAC=javac"
    set "JAVA=java"
) else (
    set "JAVAC=%JAVA_HOME%\bin\javac.exe"
    set "JAVA=%JAVA_HOME%\bin\java.exe"
)

REM Directories
set "SRC_MAIN=src\main\java"
set "SRC_TEST=src\test\java"
set "OUT_MAIN=out\main"
set "OUT_TEST=out\test"
set "LIB_DIR=lib"
set "RECORDINGS_DIR=recordings"

REM Main class
set "MAIN_CLASS=com.template.Main"

REM JFR settings
set "JFR_DURATION=60s"
set "JFR_SETTINGS=profile"

REM Get timestamp for JFR output
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set "TIMESTAMP=%datetime:~0,8%-%datetime:~8,6%"
set "JFR_OUTPUT=%RECORDINGS_DIR%\app-%TIMESTAMP%.jfr"

REM Parse command line argument
if "%1"=="" goto :compile
if "%1"=="compile" goto :compile
if "%1"=="run" goto :run
if "%1"=="compile-test" goto :compile-test
if "%1"=="test" goto :test
if "%1"=="profile" goto :profile
if "%1"=="clean" goto :clean
if "%1"=="help" goto :help
echo Unknown target: %1
goto :help

:compile
echo Compiling main sources...
if not exist "%OUT_MAIN%" mkdir "%OUT_MAIN%"

REM Find all .java files in src/main/java
set "SOURCES_FILE=%TEMP%\java_sources_%RANDOM%.txt"
dir /s /b "%SRC_MAIN%\*.java" > "%SOURCES_FILE%"

REM Check if any sources were found
for %%A in ("%SOURCES_FILE%") do set SIZE=%%~zA
if %SIZE%==0 (
    echo No source files found in %SRC_MAIN%
    del "%SOURCES_FILE%"
    exit /b 1
)

"%JAVAC%" -d "%OUT_MAIN%" -sourcepath "%SRC_MAIN%" @"%SOURCES_FILE%"
set "COMPILE_RESULT=%ERRORLEVEL%"
del "%SOURCES_FILE%"

if %COMPILE_RESULT% neq 0 (
    echo Compilation failed.
    exit /b 1
)
echo Compilation complete.
if "%1"=="compile" exit /b 0
goto :eof

:run
call :compile
if %ERRORLEVEL% neq 0 exit /b 1
echo Running application...
"%JAVA%" -cp "%OUT_MAIN%" %MAIN_CLASS%
exit /b %ERRORLEVEL%

:compile-test
call :compile
if %ERRORLEVEL% neq 0 exit /b 1
echo Compiling test sources...
if not exist "%OUT_TEST%" mkdir "%OUT_TEST%"

REM Check if JUnit JAR exists
if not exist "%LIB_DIR%\junit-platform-console-standalone-6.0.0.jar" (
    echo Error: JUnit JAR not found. Please download junit-platform-console-standalone-6.0.0.jar to %LIB_DIR%\
    exit /b 1
)

REM Find all .java files in src/test/java
set "TEST_SOURCES_FILE=%TEMP%\java_test_sources_%RANDOM%.txt"
dir /s /b "%SRC_TEST%\*.java" > "%TEST_SOURCES_FILE%" 2>nul

REM Check if any test sources were found
for %%A in ("%TEST_SOURCES_FILE%") do set SIZE=%%~zA
if %SIZE%==0 (
    echo No test source files found in %SRC_TEST%
    del "%TEST_SOURCES_FILE%"
    exit /b 1
)

set "TEST_CLASSPATH=%OUT_TEST%;%OUT_MAIN%;%LIB_DIR%\junit-platform-console-standalone-6.0.0.jar"
"%JAVAC%" -d "%OUT_TEST%" -cp "%TEST_CLASSPATH%" -sourcepath "%SRC_TEST%" @"%TEST_SOURCES_FILE%"
set "COMPILE_RESULT=%ERRORLEVEL%"
del "%TEST_SOURCES_FILE%"

if %COMPILE_RESULT% neq 0 (
    echo Test compilation failed.
    exit /b 1
)
echo Test compilation complete.
if "%1"=="compile-test" exit /b 0
goto :eof

:test
call :compile-test
if %ERRORLEVEL% neq 0 exit /b 1
echo Running tests...
set "TEST_CLASSPATH=%OUT_TEST%;%OUT_MAIN%"
"%JAVA%" -jar "%LIB_DIR%\junit-platform-console-standalone-6.0.0.jar" execute --class-path "%TEST_CLASSPATH%" --scan-classpath
exit /b %ERRORLEVEL%

:profile
call :compile
if %ERRORLEVEL% neq 0 exit /b 1
echo Profiling application with Java Flight Recorder...
if not exist "%RECORDINGS_DIR%" mkdir "%RECORDINGS_DIR%"
echo Recording will be saved to: %JFR_OUTPUT%
"%JAVA%" -XX:StartFlightRecording=filename=%JFR_OUTPUT%,duration=%JFR_DURATION%,settings=%JFR_SETTINGS% -cp "%OUT_MAIN%" %MAIN_CLASS%
echo Recording saved to: %JFR_OUTPUT%
echo To view the recording, use: jmc %JFR_OUTPUT% or jfr print %JFR_OUTPUT%
exit /b 0

:clean
echo Cleaning...
if exist "%OUT_MAIN%" (
    del /q "%OUT_MAIN%\*" 2>nul
    for /d %%p in ("%OUT_MAIN%\*") do rmdir "%%p" /s /q 2>nul
)
if exist "%OUT_TEST%" (
    del /q "%OUT_TEST%\*" 2>nul
    for /d %%p in ("%OUT_TEST%\*") do rmdir "%%p" /s /q 2>nul
)
if exist "%RECORDINGS_DIR%" (
    del /q "%RECORDINGS_DIR%\*" 2>nul
)
echo Clean complete.
exit /b 0

:help
echo Available targets:
echo   build.bat compile      - Compile main sources
echo   build.bat run          - Compile and run the application
echo   build.bat compile-test - Compile test sources (requires JUnit)
echo   build.bat test         - Run JUnit tests
echo   build.bat profile      - Run app with Java Flight Recorder
echo   build.bat clean        - Remove compiled classes and recordings
echo   build.bat help         - Show this help message
exit /b 0
