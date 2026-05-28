# Java Project Build Script for Windows (PowerShell)
# Uses JDK 25

param(
    [Parameter(Position=0)]
    [ValidateSet('compile', 'run', 'compile-test', 'test', 'profile', 'clean', 'help')]
    [string]$Target = 'compile'
)

# Detect JAVA_HOME - use environment variable or try to find Java
if (-not $env:JAVA_HOME) {
    Write-Host "JAVA_HOME not set. Attempting to find Java..." -ForegroundColor Yellow
    $javaPath = Get-Command java -ErrorAction SilentlyContinue
    if ($javaPath) {
        Write-Host "Found Java at: $($javaPath.Source)" -ForegroundColor Green
        $script:JAVAC = "javac"
        $script:JAVA = "java"
    } else {
        Write-Error "Java not found. Please set JAVA_HOME or add Java to PATH."
        exit 1
    }
} else {
    $script:JAVAC = Join-Path $env:JAVA_HOME "bin\javac.exe"
    $script:JAVA = Join-Path $env:JAVA_HOME "bin\java.exe"
}

# Directories
$SRC_MAIN = "src\main\java"
$SRC_TEST = "src\test\java"
$OUT_MAIN = "out\main"
$OUT_TEST = "out\test"
$LIB_DIR = "lib"
$RECORDINGS_DIR = "recordings"

# Main class
$MAIN_CLASS = "com.template.Main"

# JFR settings
$JFR_DURATION = "60s"
$JFR_SETTINGS = "profile"

# Get timestamp for JFR output
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$JFR_OUTPUT = Join-Path $RECORDINGS_DIR "app-$TIMESTAMP.jfr"

function Compile-Main {
    Write-Host "Compiling main sources..." -ForegroundColor Cyan

    if (-not (Test-Path $OUT_MAIN)) {
        New-Item -ItemType Directory -Path $OUT_MAIN -Force | Out-Null
    }

    # Find all .java files in src/main/java
    $sourceFiles = Get-ChildItem -Path $SRC_MAIN -Filter "*.java" -Recurse -File

    if ($sourceFiles.Count -eq 0) {
        Write-Error "No source files found in $SRC_MAIN"
        return $false
    }

    # Create temporary file with source paths
    $sourcesFile = [System.IO.Path]::GetTempFileName()
    $sourceFiles | ForEach-Object { $_.FullName } | Out-File -FilePath $sourcesFile -Encoding ASCII

    # Compile
    & $JAVAC -d $OUT_MAIN -sourcepath $SRC_MAIN "@$sourcesFile"
    $result = $LASTEXITCODE

    Remove-Item $sourcesFile

    if ($result -ne 0) {
        Write-Error "Compilation failed."
        return $false
    }

    Write-Host "Compilation complete." -ForegroundColor Green
    return $true
}

function Run-Application {
    if (-not (Compile-Main)) {
        exit 1
    }

    Write-Host "Running application..." -ForegroundColor Cyan
    & $JAVA -cp $OUT_MAIN $MAIN_CLASS
    exit $LASTEXITCODE
}

function Compile-Tests {
    if (-not (Compile-Main)) {
        exit 1
    }

    Write-Host "Compiling test sources..." -ForegroundColor Cyan

    if (-not (Test-Path $OUT_TEST)) {
        New-Item -ItemType Directory -Path $OUT_TEST -Force | Out-Null
    }

    # Check if JUnit JAR exists
    $junitJar = Join-Path $LIB_DIR "junit-platform-console-standalone-6.0.0.jar"
    if (-not (Test-Path $junitJar)) {
        Write-Error "JUnit JAR not found. Please download junit-platform-console-standalone-6.0.0.jar to $LIB_DIR\"
        return $false
    }

    # Find all .java files in src/test/java
    $testFiles = Get-ChildItem -Path $SRC_TEST -Filter "*.java" -Recurse -File -ErrorAction SilentlyContinue

    if (-not $testFiles -or $testFiles.Count -eq 0) {
        Write-Error "No test source files found in $SRC_TEST"
        return $false
    }

    # Create temporary file with test source paths
    $testSourcesFile = [System.IO.Path]::GetTempFileName()
    $testFiles | ForEach-Object { $_.FullName } | Out-File -FilePath $testSourcesFile -Encoding ASCII

    # Compile tests
    $testClasspath = "$OUT_TEST;$OUT_MAIN;$junitJar"
    & $JAVAC -d $OUT_TEST -cp $testClasspath -sourcepath $SRC_TEST "@$testSourcesFile"
    $result = $LASTEXITCODE

    Remove-Item $testSourcesFile

    if ($result -ne 0) {
        Write-Error "Test compilation failed."
        return $false
    }

    Write-Host "Test compilation complete." -ForegroundColor Green
    return $true
}

function Run-Tests {
    if (-not (Compile-Tests)) {
        exit 1
    }

    Write-Host "Running tests..." -ForegroundColor Cyan
    $testClasspath = "$OUT_TEST;$OUT_MAIN"
    $junitJar = Join-Path $LIB_DIR "junit-platform-console-standalone-6.0.0.jar"

    & $JAVA -jar $junitJar execute --class-path $testClasspath --scan-classpath
    exit $LASTEXITCODE
}

function Profile-Application {
    if (-not (Compile-Main)) {
        exit 1
    }

    Write-Host "Profiling application with Java Flight Recorder..." -ForegroundColor Cyan

    if (-not (Test-Path $RECORDINGS_DIR)) {
        New-Item -ItemType Directory -Path $RECORDINGS_DIR -Force | Out-Null
    }

    Write-Host "Recording will be saved to: $JFR_OUTPUT" -ForegroundColor Yellow

    & $JAVA "-XX:StartFlightRecording=filename=$JFR_OUTPUT,duration=$JFR_DURATION,settings=$JFR_SETTINGS" -cp $OUT_MAIN $MAIN_CLASS

    Write-Host "Recording saved to: $JFR_OUTPUT" -ForegroundColor Green
    Write-Host "To view the recording, use: jmc $JFR_OUTPUT or jfr print $JFR_OUTPUT" -ForegroundColor Cyan
    exit 0
}

function Clean-Build {
    Write-Host "Cleaning..." -ForegroundColor Cyan

    if (Test-Path $OUT_MAIN) {
        Get-ChildItem -Path $OUT_MAIN -Recurse | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    }

    if (Test-Path $OUT_TEST) {
        Get-ChildItem -Path $OUT_TEST -Recurse | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
    }

    if (Test-Path $RECORDINGS_DIR) {
        Get-ChildItem -Path $RECORDINGS_DIR -Recurse | Remove-Item -Force -ErrorAction SilentlyContinue
    }

    Write-Host "Clean complete." -ForegroundColor Green
    exit 0
}

function Show-Help {
    Write-Host @"
Available targets:
  .\build.ps1 compile      - Compile main sources
  .\build.ps1 run          - Compile and run the application
  .\build.ps1 compile-test - Compile test sources (requires JUnit)
  .\build.ps1 test         - Run JUnit tests
  .\build.ps1 profile      - Run app with Java Flight Recorder
  .\build.ps1 clean        - Remove compiled classes and recordings
  .\build.ps1 help         - Show this help message
"@ -ForegroundColor Cyan
    exit 0
}

# Execute the requested target
switch ($Target) {
    'compile' {
        if (Compile-Main) { exit 0 } else { exit 1 }
    }
    'run' { Run-Application }
    'compile-test' {
        if (Compile-Tests) { exit 0 } else { exit 1 }
    }
    'test' { Run-Tests }
    'profile' { Profile-Application }
    'clean' { Clean-Build }
    'help' { Show-Help }
}
