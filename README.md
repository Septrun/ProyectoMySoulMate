# Java Template

## Project Structure

```
java-template/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/template/
│   │           └── Main.java
│   └── test/
│       └── java/
│           └── com/template/
│               └── MainTest.java
├── lib/                    # JUnit JAR for testing
├── out/
│   ├── main/              # Compiled main classes
│   └── test/              # Compiled test classes
├── recordings/            # JFR recordings output
├── Makefile               # Build script for Linux/macOS
├── build.bat              # Build script for Windows (Batch)
├── build.ps1              # Build script for Windows (PowerShell)
└── README.md
```

## Requirements

- **JDK 25** (OpenJDK 25.0.2 or compatible)
- **Build tool:**
  - **Linux/macOS:** Make
  - **Windows:** Batch (build.bat) or PowerShell (build.ps1)

## Setup

### 1. Verify JDK Installation

The build scripts expect JDK 25 to be available. They will use:
- `JAVA_HOME` environment variable if set
- Otherwise, will attempt to find Java in your system PATH

To verify your JDK version:
```bash
java --version
```

### 2. Set JAVA_HOME (Optional but Recommended)

**Linux/macOS:**
```bash
export JAVA_HOME=/path/to/jdk-25
```

**Windows (Command Prompt):**
```cmd
set JAVA_HOME=C:\Path\To\jdk-25
```

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME = "C:\Path\To\jdk-25"
```

## Usage

Choose the appropriate build script for your operating system:

### Linux/macOS (Makefile)

#### Compile the Project
```bash
make compile
```

#### Run the Application
```bash
make run
```

#### Compile Tests
```bash
make compile-test
```

#### Run Tests
```bash
make test
```

#### Profile with Java Flight Recorder
```bash
make profile
```

#### Clean Build Artifacts
```bash
make clean
```

#### Help
```bash
make help
```

---

### Windows (Batch Script)

#### Compile the Project
```cmd
build.bat compile
```

#### Run the Application
```cmd
build.bat run
```

#### Compile Tests
```cmd
build.bat compile-test
```

#### Run Tests
```cmd
build.bat test
```

#### Profile with Java Flight Recorder
```cmd
build.bat profile
```

#### Clean Build Artifacts
```cmd
build.bat clean
```

#### Help
```cmd
build.bat help
```

---

### Windows (PowerShell Script)

#### Compile the Project
```powershell
.\build.ps1 compile
```

#### Run the Application
```powershell
.\build.ps1 run
```

#### Compile Tests
```powershell
.\build.ps1 compile-test
```

#### Run Tests
```powershell
.\build.ps1 test
```

#### Profile with Java Flight Recorder
```powershell
.\build.ps1 profile
```

#### Clean Build Artifacts
```powershell
.\build.ps1 clean
```

#### Help
```powershell
.\build.ps1 help
```

---

## Features

### Compile
Compiles all Java sources in `src/main/java/` to `out/main/`.

### Run
Compiles (if needed) and runs the main application.

### Compile Tests
Compiles test sources. Requires JUnit JAR in `lib/` directory.

### Run Tests
Compiles and runs all JUnit tests.

### Profile with Java Flight Recorder
Runs the application with JFR profiling enabled. The recording will be saved to `recordings/` directory with a timestamp.

**JFR Configuration:**
- Duration: 60 seconds (configurable)
- Settings: `profile` (higher overhead, more detailed profiling)
- Output: Timestamped `.jfr` files in `recordings/`

### Clean
Removes all compiled classes and JFR recordings.

## Notes for Windows Users

- **PowerShell Execution Policy:** If you encounter an error running `build.ps1`, you may need to adjust your execution policy:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
  ```

- **Path Separators:** The Windows scripts automatically handle Windows path separators (`\`) and classpath separators (`;`).

- **WSL Alternative:** If you prefer, you can use Windows Subsystem for Linux (WSL) and run the Makefile directly.
