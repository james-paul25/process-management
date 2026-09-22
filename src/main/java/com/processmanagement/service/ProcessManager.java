package com.processmanagement.service;

import com.processmanagement.model.Process;
import com.processmanagement.model.ProcessState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ProcessManager {

    private final ObservableList<Process> processes = FXCollections.observableArrayList();
    private final AtomicInteger nextPid = new AtomicInteger(1000);
    private final Map<Integer, java.lang.Process> runningOsProcesses = new ConcurrentHashMap<>();

    public ObservableList<Process> getProcesses() {
        return processes;
    }

    public Process createProcess(String name, int arrivalTime, int burstTime, int priority) {
        Process process = new Process(nextPid.getAndIncrement(), name, arrivalTime, burstTime, priority);
        processes.add(process);
        return process;
    }

    public Optional<Process> findByPid(int pid) {
        return processes.stream().filter(p -> p.getPid() == pid).findFirst();
    }

    public void start(Process p) {
        if (p.getState() == ProcessState.NEW || p.getState() == ProcessState.READY
                || p.getState() == ProcessState.WAITING) {
            p.setState(ProcessState.RUNNING);
        }
    }

    public void moveToReady(Process p) {
        if (p.getState() != ProcessState.TERMINATED) {
            p.setState(ProcessState.READY);
        }
    }

    public void stop(Process p) {
        if (p.getState() == ProcessState.RUNNING) {
            p.setState(ProcessState.WAITING);
        }
    }

    public void terminate(Process p) {
        p.setState(ProcessState.TERMINATED);
        p.setRemainingTime(0);
    }

    /**
     * SIMULATED execution: does NOT spawn a real OS process. It only animates
     * the process through RUNNING -> TERMINATED over a period proportional to
     * its burst time, purely to visually demonstrate execution.
     */
    public void executeSimulated(Process p, Runnable onFinished) {
        if (p.getState() == ProcessState.TERMINATED)
            return;
        p.setState(ProcessState.RUNNING);

        double seconds = Math.max(0.4, p.getBurstTime() * 0.3);
        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> {
            p.setRemainingTime(0);
            p.setState(ProcessState.TERMINATED);
            if (onFinished != null)
                onFinished.run();
        });
        pause.play();
    }

    /**
     * REAL OS execution: launches an actual operating-system process using
     * ProcessBuilder. The PID reported here (Process#pid()) is assigned by
     * the operating system and is DIFFERENT from the simulated PID this app
     * assigns.
     *
     * Limitations:
     * - The launched command is short-lived and platform-dependent
     * (sleep on Linux/macOS, timeout on Windows).
     * - stop()/resume() on this app's model only change our own state; they
     * do not pause or resume the real OS process' scheduling.
     * - Killing uses Process#destroy()/destroyForcibly(), which map to OS
     * signals (SIGTERM/SIGKILL on Unix) rather than our own state machine.
     */
    public java.lang.Process executeReal(Process p, List<String> command,
            Consumer<String> onOutputLine,
            Runnable onFinished) throws IOException {
        p.setState(ProcessState.RUNNING);
        p.setExitCode(Integer.MIN_VALUE);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        java.lang.Process osProcess = builder.start();
        p.setOsPid(osProcess.pid());
        runningOsProcesses.put(p.getPid(), osProcess);

        // Read output on a background thread; ProcessBuilder does not do this
        // for you, and an unread output buffer can deadlock a long-running
        // process once the OS pipe fills up.
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(osProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    if (onOutputLine != null) {
                        Platform.runLater(() -> onOutputLine.accept(finalLine));
                    }
                }
            } catch (IOException ignored) {
                // Stream closes when the process exits; nothing to act on.
            }
        }, "os-process-output-" + p.getPid());
        outputReader.setDaemon(true);
        outputReader.start();

        osProcess.onExit().thenAccept(finished -> Platform.runLater(() -> {
            p.setState(ProcessState.TERMINATED);
            p.setRemainingTime(0);
            p.setExitCode(finished.exitValue());
            runningOsProcesses.remove(p.getPid());
            if (onFinished != null)
                onFinished.run();
        }));

        return osProcess;
    }

    /**
     * Builds the default command used when the user doesn't type a custom
     * one: a short-lived, platform-appropriate command, same behavior as
     * the original hardcoded version.
     */
    public List<String> defaultCommandFor(Process p) {
        String os = System.getProperty("os.name").toLowerCase();
        int seconds = Math.max(1, p.getBurstTime());
        return os.contains("win")
                ? List.of("timeout", String.valueOf(seconds))
                : List.of("sleep", String.valueOf(seconds));
    }

    /** Kills the real OS process backing this Process, if one is running. */
    public void killReal(Process p, boolean force) {
        java.lang.Process osProcess = runningOsProcesses.get(p.getPid());
        if (osProcess == null)
            return;
        if (force) {
            osProcess.destroyForcibly(); // maps to SIGKILL on Unix
        } else {
            osProcess.destroy(); // maps to SIGTERM on Unix
        }
    }
}