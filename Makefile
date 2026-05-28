# Java Project Makefile
# Uses JDK 25

# Detect JAVA_HOME - prefer environment variable, fallback to detected JDK
JAVA_HOME ?= /opt/homebrew/opt/openjdk
JAVAC = $(JAVA_HOME)/bin/javac
JAVA = $(JAVA_HOME)/bin/java

# Directories
SRC_MAIN = src/main/java
SRC_TEST = src/test/java
OUT_MAIN = out/main
OUT_TEST = out/test
LIB_DIR = lib
RECORDINGS_DIR = recordings

# Source files
MAIN_SOURCES = $(shell find $(SRC_MAIN) -name "*.java")
TEST_SOURCES = $(shell find $(SRC_TEST) -name "*.java")

# Classpath
MAIN_CLASSPATH = $(OUT_MAIN)
TEST_CLASSPATH = $(OUT_TEST):$(OUT_MAIN):$(LIB_DIR)/junit-platform-console-standalone-6.0.0.jar

# Main class
MAIN_CLASS = com.template.Main

# JFR settings
JFR_DURATION = 60s
JFR_SETTINGS = profile
JFR_OUTPUT = $(RECORDINGS_DIR)/app-$(shell date +%Y%m%d-%H%M%S).jfr

.PHONY: all compile run compile-test test profile clean help

# Default target
all: compile

# Compile main sources
compile:
	@echo "Compiling main sources..."
	@mkdir -p $(OUT_MAIN)
	$(JAVAC) -d $(OUT_MAIN) -sourcepath $(SRC_MAIN) $(MAIN_SOURCES)
	@echo "Compilation complete."

# Run the application
run: compile
	@echo "Running application..."
	$(JAVA) -cp $(MAIN_CLASSPATH) $(MAIN_CLASS)

# Compile test sources
compile-test: compile
	@echo "Compiling test sources..."
	@mkdir -p $(OUT_TEST)
	@if [ ! -f $(LIB_DIR)/junit-platform-console-standalone-6.0.0.jar ]; then \
		echo "Error: JUnit JAR not found. Please download junit-platform-console-standalone-6.0.0.jar to $(LIB_DIR)/"; \
		exit 1; \
	fi
	$(JAVAC) -d $(OUT_TEST) -cp $(TEST_CLASSPATH) -sourcepath $(SRC_TEST) $(TEST_SOURCES)
	@echo "Test compilation complete."

# Run tests
test: compile-test
	@echo "Running tests..."
	$(JAVA) -jar $(LIB_DIR)/junit-platform-console-standalone-6.0.0.jar execute \
		--class-path $(OUT_TEST):$(OUT_MAIN) \
		--scan-classpath

# Profile with Java Flight Recorder
profile: compile
	@echo "Profiling application with Java Flight Recorder..."
	@mkdir -p $(RECORDINGS_DIR)
	@echo "Recording will be saved to: $(JFR_OUTPUT)"
	$(JAVA) \
		-XX:StartFlightRecording=filename=$(JFR_OUTPUT),duration=$(JFR_DURATION),settings=$(JFR_SETTINGS) \
		-cp $(MAIN_CLASSPATH) \
		$(MAIN_CLASS)
	@echo "Recording saved to: $(JFR_OUTPUT)"
	@echo "To view the recording, use: jmc $(JFR_OUTPUT) or jfr print $(JFR_OUTPUT)"

# Clean compiled classes and recordings
clean:
	@echo "Cleaning..."
	rm -rf $(OUT_MAIN)/* $(OUT_TEST)/* $(RECORDINGS_DIR)/*
	@echo "Clean complete."

# Help
help:
	@echo "Available targets:"
	@echo "  make compile      - Compile main sources"
	@echo "  make run          - Compile and run the application"
	@echo "  make compile-test - Compile test sources (requires JUnit)"
	@echo "  make test         - Run JUnit tests"
	@echo "  make profile      - Run app with Java Flight Recorder"
	@echo "  make clean        - Remove compiled classes and recordings"
	@echo "  make help         - Show this help message"
