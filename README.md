# Process Management Simulator

A JavaFX desktop application that demonstrates core operating-system concepts:
the process lifecycle, CPU scheduling algorithms, and memory swapping.

Everything except the "Execute (Real OS Process)" button is a **simulation** —
the app models these concepts rather than manipulating your machine's actual
scheduler or swap file.

---

## Features

| Tab | What it demonstrates |
|-----|----------------------|
| **Processes** | Creating processes, the NEW → READY → RUNNING → WAITING → TERMINATED state machine, and launching a real OS process for comparison |
| **Scheduling** | FCFS, SJF, Priority, and Round Robin, with per-process metrics, averages, and a Gantt chart |
| **Memory & Swapping** | A fixed-capacity main memory (3 processes) and a swap area, with manual load / swap-out / swap-in |

---

## Requirements

| Requirement | Version | Notes |
|-------------|---------|-------|
| JDK | 21 or newer | Must be a **JDK**, not a JRE |
| Maven | 3.8+ | |
| JavaFX | 21.0.2 | Downloaded automatically by Maven — do **not** install it separately |
| Display | Any | The app opens a GUI window; it will not run on a headless server |

Check what you have:

```bash
java -version    # should report 21 or higher
mvn -version
```

### Linux: install the JDK and Maven

Debian / Ubuntu:

```bash
sudo apt update
sudo apt install openjdk-21-jdk maven
```

Fedora:

```bash
sudo dnf install java-21-openjdk-devel maven
```

Arch:

```bash
sudo pacman -S jdk21-openjdk maven
```

If you have several JDKs installed, select 21:

```bash
sudo update-alternatives --config java     # Debian/Ubuntu
sudo alternatives --config java            # Fedora
```

### macOS

```bash
brew install openjdk@21 maven
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk \
             /Library/Java/JavaVirtualMachines/openjdk-21.jdk
```

### Windows

Install a JDK 21 build (Temurin, Zulu, or Oracle) and Maven, then confirm
`JAVA_HOME` points at the JDK 21 directory and that `mvn -version` works from
a new terminal.

---

## Install and run

```bash
git clone <your-repo-url> process-management
cd process-management
mvn clean javafx:run
```

The first run downloads JavaFX and its native libraries into `~/.m2`, so it
takes longer than later runs. A window titled *Process Management Simulator*
should open.

To compile without launching the GUI:

```bash
mvn clean compile
```

To package a jar:

```bash
mvn clean package
```

Note that the resulting jar is not self-contained — it does not bundle the
JavaFX runtime. Use `mvn javafx:run` for normal use, or add
`javafx-maven-plugin`'s `jlink` goal if you need a standalone distribution.

---

## Project layout

```
src/main/java/com/processmanagement/
├── Main.java                   # JavaFX entry point, loads main.fxml
├── controller/
│   └── MainController.java     # All UI event handling
├── model/
│   ├── Process.java            # The simulated PCB (JavaFX properties)
│   ├── ProcessState.java       # NEW, READY, RUNNING, WAITING, SWAPPED_OUT, TERMINATED
│   ├── ProcessResult.java      # Per-process scheduling metrics
│   ├── GanttEntry.java         # One bar in the Gantt chart
│   └── SchedulingResult.java   # Results + averages for one run
├── scheduler/
│   ├── Scheduler.java          # Common interface
│   ├── FCFSScheduler.java
│   ├── SJFScheduler.java
│   ├── PriorityScheduler.java
│   └── RoundRobinScheduler.java
└── service/
    ├── ProcessManager.java     # Process creation, state transitions, execution
    └── MemoryManager.java      # Main memory vs. swap space

src/main/resources/com/processmanagement/
├── main.fxml                   # UI layout
└── style.css                   # Styling
```

---

## Conventions worth knowing

- **Simulated PIDs start at 1000** and increment. They are assigned by this
  app and are unrelated to real OS PIDs.
- **Lower priority number = higher priority.** Priority 1 runs before
  priority 5.
- **Main memory holds 3 processes.** New processes are auto-loaded while
  there is room; beyond that you must swap something out first. Change the
  number in `MainController`:

  ```java
  private final MemoryManager memoryManager = new MemoryManager(3);
  ```

- **Scheduling uses every process in the table**, including terminated ones.
  It reads arrival/burst/priority values and computes results on paper — it
  does not change any process's state.

---

## Troubleshooting

**`reference to Process is ambiguous`**

A file imports `com.processmanagement.model.*` without a single-type import
for `Process`, so it collides with `java.lang.Process`. Every file using the
wildcard must also declare:

```java
import com.processmanagement.model.Process;
```

**`duplicate class: src.main.java.com.processmanagement...`**

A source file's `package` declaration includes the `src/main/java` path
prefix. Package declarations must be relative to the source root, e.g.
`package com.processmanagement.model;`. Check with:

```bash
grep -rn "^package" src/main/java | grep -v ":package com\.processmanagement"
```

**`Error: JavaFX runtime components are missing`**

You ran the jar directly. Use `mvn javafx:run`.

**`Could not launch real OS process`**

The real-execution button runs `sleep` on Linux/macOS and `timeout` on
Windows. If that command isn't on your `PATH`, only the simulated execution
will work.

**`release version 21 not supported`**

Maven is using an older JDK. Point `JAVA_HOME` at your JDK 21 install and
re-run.