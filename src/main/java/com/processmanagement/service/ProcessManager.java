package src.main.java.com.processmanagement.service;

import com.processmanagement.model.Process;
import com.processmanagement.model.ProcessState;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessManager {

    private final ObservableList<Process> processes = FXCollections.observableArrayList();
    private final AtomicInteger nextPid = new AtomicInteger(1000);

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
        if (p.getState() == ProcessState.TERMINATED) return;
        p.setState(ProcessState.RUNNING);

        double seconds = Math.max(0.4, p.getBurstTime() * 0.3);
        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));
        pause.setOnFinished(e -> {
            p.setRemainingTime(0);
            p.setState(ProcessState.TERMINATED);
            if (onFinished != null) onFinished.run();
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
     *  - The launched command is short-lived and platform-dependent
     *    (sleep on Linux/macOS, timeout on Windows).
     *  - stop()/resume() on this app's model only change our own state; they
     *    do not pause or resume the real OS process' scheduling.
     *  - Killing uses Process#destroy()/destroyForcibly(), which map to OS
     *    signals (SIGTERM/SIGKILL on Unix) rather than our own state machine.
     */
    public java.lang.Process executeReal(Process p, Runnable onFinished) throws IOException {
        p.setState(ProcessState.RUNNING);

        String os = System.getProperty("os.name").toLowerCase();
        int seconds = Math.max(1, p.getBurstTime());
        ProcessBuilder builder = os.contains("win")
                ? new ProcessBuilder("timeout", String.valueOf(seconds))
                : new ProcessBuilder("sleep", String.valueOf(seconds));
        builder.redirectErrorStream(true);

        java.lang.Process osProcess = builder.start();
        p.setOsPid(osProcess.pid());

        osProcess.onExit().thenAccept(finished -> Platform.runLater(() -> {
            p.setState(ProcessState.TERMINATED);
            p.setRemainingTime(0);
            if (onFinished != null) onFinished.run();
        }));

        return osProcess;
    }
}