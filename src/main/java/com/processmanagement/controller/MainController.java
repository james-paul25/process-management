package com.processmanagement.controller;

import com.processmanagement.model.*;
import com.processmanagement.model.Process;
import com.processmanagement.scheduler.*;
import com.processmanagement.service.MemoryManager;
import com.processmanagement.service.ProcessManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainController implements Initializable {

    // ----- Processes tab -----
    @FXML private TextField nameField;
    @FXML private TextField arrivalField;
    @FXML private TextField burstField;
    @FXML private TextField priorityField;

    @FXML private TableView<Process> processTable;
    @FXML private TableColumn<Process, Integer> colPid;
    @FXML private TableColumn<Process, String> colName;
    @FXML private TableColumn<Process, ProcessState> colState;
    @FXML private TableColumn<Process, Integer> colArrival;
    @FXML private TableColumn<Process, Integer> colBurst;
    @FXML private TableColumn<Process, Integer> colRemaining;
    @FXML private TableColumn<Process, Integer> colPriority;
    @FXML private TableColumn<Process, Long> colOsPid;

    @FXML private TextField actionPidField;
    @FXML private Label processStatusLabel;

    @FXML private Label stateNew;
    @FXML private Label stateReady;
    @FXML private Label stateRunning;
    @FXML private Label stateWaiting;
    @FXML private Label stateTerminated;

    // ----- Scheduling tab -----
    @FXML private ComboBox<String> algorithmCombo;
    @FXML private TextField quantumField;
    @FXML private TableView<ProcessResult> resultTable;
    @FXML private TableColumn<ProcessResult, Integer> colResPid;
    @FXML private TableColumn<ProcessResult, String> colResName;
    @FXML private TableColumn<ProcessResult, Integer> colResArrival;
    @FXML private TableColumn<ProcessResult, Integer> colResBurst;
    @FXML private TableColumn<ProcessResult, Integer> colResPriority;
    @FXML private TableColumn<ProcessResult, Integer> colResCompletion;
    @FXML private TableColumn<ProcessResult, Integer> colResWaiting;
    @FXML private TableColumn<ProcessResult, Integer> colResTurnaround;
    @FXML private TableColumn<ProcessResult, Integer> colResResponse;
    @FXML private Label avgWaitingLabel;
    @FXML private Label avgTurnaroundLabel;
    @FXML private Label avgResponseLabel;
    @FXML private HBox ganttBox;

    // ----- Memory tab -----
    @FXML private Label memoryCapacityLabel;
    @FXML private ListView<Process> mainMemoryList;
    @FXML private ListView<Process> swapList;
    @FXML private TextField swapPidField;
    @FXML private Label memoryStatusLabel;

    private final ProcessManager processManager = new ProcessManager();
    private final MemoryManager memoryManager = new MemoryManager(3);

    private static final String[] GANTT_COLORS = {
            "#2563eb", "#16a34a", "#ea580c", "#9333ea", "#dc2626", "#0d9488", "#ca8a04"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProcessTable();
        setupResultTable();
        setupSchedulingControls();
        setupMemoryTab();

        processTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> updateStateDiagram(newVal));
    }

    // ---------- Setup ----------

    private void setupProcessTable() {
        processTable.setItems(processManager.getProcesses());

        colPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colOsPid.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getOsPid()));
        colOsPid.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null || value < 0 ? "-" : String.valueOf(value));
            }
        });
    }

    private void setupResultTable() {
        colResPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        colResName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colResArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colResBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        colResPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colResCompletion.setCellValueFactory(new PropertyValueFactory<>("completionTime"));
        colResWaiting.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        colResTurnaround.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        colResResponse.setCellValueFactory(new PropertyValueFactory<>("responseTime"));
    }

    private void setupSchedulingControls() {
        algorithmCombo.setItems(FXCollections.observableArrayList(
                "First Come First Served (FCFS)",
                "Shortest Job First (SJF)",
                "Round Robin",
                "Priority Scheduling"
        ));
        algorithmCombo.getSelectionModel().selectFirst();
    }

    private void setupMemoryTab() {
        memoryCapacityLabel.setText("Main memory capacity: " + memoryManager.getCapacity() + " processes");
        mainMemoryList.setItems(memoryManager.getMainMemory());
        swapList.setItems(memoryManager.getSwapSpace());
    }

    // ---------- Processes tab actions ----------

    @FXML
    private void handleAddProcess() {
        try {
            String name = nameField.getText().isBlank()
                    ? "P" + (processManager.getProcesses().size() + 1)
                    : nameField.getText().trim();
            int arrival = parseIntOrDefault(arrivalField.getText(), 0);
            int burst = Integer.parseInt(burstField.getText().trim());
            int priority = parseIntOrDefault(priorityField.getText(), 1);

            if (burst <= 0) {
                showStatus(processStatusLabel, "Burst time must be greater than zero.", true);
                return;
            }

            Process p = processManager.createProcess(name, arrival, burst, priority);
            memoryManager.loadToMemory(p); // auto-place into memory while there is room

            nameField.clear();
            arrivalField.clear();
            burstField.clear();
            priorityField.clear();

            showStatus(processStatusLabel, "Created process PID " + p.getPid() + ".", false);
        } catch (NumberFormatException e) {
            showStatus(processStatusLabel, "Arrival, Burst and Priority must be valid integers.", true);
        }
    }

    @FXML
    private void handleStart() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            processManager.start(p);
            showStatus(processStatusLabel, "PID " + p.getPid() + " started (RUNNING).", false);
        });
    }

    @FXML
    private void handleStop() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            processManager.stop(p);
            showStatus(processStatusLabel, "PID " + p.getPid() + " stopped (WAITING).", false);
        });
    }

    @FXML
    private void handleResume() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            processManager.moveToReady(p);
            showStatus(processStatusLabel, "PID " + p.getPid() + " moved to READY.", false);
        });
    }

    @FXML
    private void handleTerminate() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            processManager.terminate(p);
            showStatus(processStatusLabel, "PID " + p.getPid() + " TERMINATED.", false);
        });
    }

    @FXML
    private void handleExecuteSimulated() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            showStatus(processStatusLabel, "Executing PID " + p.getPid() + " (simulated)...", false);
            processManager.executeSimulated(p,
                    () -> showStatus(processStatusLabel, "PID " + p.getPid() + " finished (simulated).", false));
        });
    }

    @FXML
    private void handleExecuteReal() {
        withSelectedOrTypedProcess(actionPidField, processStatusLabel, p -> {
            try {
                java.lang.Process osProcess = processManager.executeReal(p,
                        () -> showStatus(processStatusLabel,
                                "PID " + p.getPid() + " (OS PID " + p.getOsPid() + ") finished.", false));
                showStatus(processStatusLabel,
                        "Launched real OS process for PID " + p.getPid() + " -> OS PID " + osProcess.pid(), false);
            } catch (Exception e) {
                showStatus(processStatusLabel, "Could not launch real OS process: " + e.getMessage(), true);
            }
        });
    }

    private void updateStateDiagram(Process p) {
        List<Label> all = List.of(stateNew, stateReady, stateRunning, stateWaiting, stateTerminated);
        all.forEach(l -> l.getStyleClass().remove("state-box-active"));
        if (p == null) return;

        Label active = switch (p.getState()) {
            case NEW -> stateNew;
            case READY -> stateReady;
            case RUNNING -> stateRunning;
            case WAITING -> stateWaiting;
            case SWAPPED_OUT -> stateReady;
            case TERMINATED -> stateTerminated;
        };
        active.getStyleClass().add("state-box-active");
    }

    // ---------- Scheduling tab actions ----------

    @FXML
    private void handleRunScheduling() {
        ObservableList<Process> processes = processManager.getProcesses();
        if (processes.isEmpty()) {
            showStatus(processStatusLabel, "Add at least one process before running scheduling.", true);
            return;
        }

        String algorithm = algorithmCombo.getValue();
        Scheduler scheduler;
        try {
            scheduler = switch (algorithm) {
                case "First Come First Served (FCFS)" -> new FCFSScheduler();
                case "Shortest Job First (SJF)" -> new SJFScheduler();
                case "Priority Scheduling" -> new PriorityScheduler();
                case "Round Robin" -> new RoundRobinScheduler(parseIntOrDefault(quantumField.getText(), 2));
                default -> new FCFSScheduler();
            };
        } catch (IllegalArgumentException e) {
            showStatus(processStatusLabel, e.getMessage(), true);
            return;
        }

        SchedulingResult result = scheduler.schedule(processes);

        resultTable.setItems(FXCollections.observableArrayList(result.getResults()));
        avgWaitingLabel.setText(String.format(Locale.US, "Average Waiting Time: %.2f", result.getAverageWaitingTime()));
        avgTurnaroundLabel.setText(String.format(Locale.US, "Average Turnaround Time: %.2f", result.getAverageTurnaroundTime()));
        avgResponseLabel.setText(String.format(Locale.US, "Average Response Time: %.2f", result.getAverageResponseTime()));

        drawGanttChart(result.getGanttEntries());
    }

    private void drawGanttChart(List<GanttEntry> entries) {
        ganttBox.getChildren().clear();
        double pixelsPerUnit = 26;

        for (GanttEntry entry : entries) {
            double width = Math.max(24, entry.getDuration() * pixelsPerUnit);
            String color = GANTT_COLORS[Math.abs(entry.getPid()) % GANTT_COLORS.length];

            StackPane block = new StackPane();
            block.setPrefSize(width, 46);
            block.setStyle("-fx-background-color: " + color + "; -fx-border-color: #1f2937;");

            Text label = new Text(entry.getLabel() + "\n[" + entry.getStart() + "-" + entry.getEnd() + "]");
            label.setStyle("-fx-fill: white; -fx-font-size: 11px; -fx-text-alignment: center;");
            block.getChildren().add(label);

            ganttBox.getChildren().add(block);
        }
    }

    // ---------- Memory tab actions ----------

    @FXML
    private void handleLoadToMemory() {
        withTypedProcess(swapPidField, memoryStatusLabel, p -> {
            boolean ok = memoryManager.loadToMemory(p);
            showStatus(memoryStatusLabel, ok
                    ? "PID " + p.getPid() + " loaded into main memory."
                    : "Could not load PID " + p.getPid() + " (already placed or memory full).", !ok);
        });
    }

    @FXML
    private void handleSwapOut() {
        withTypedProcess(swapPidField, memoryStatusLabel, p -> {
            boolean ok = memoryManager.swapOut(p);
            showStatus(memoryStatusLabel, ok
                    ? "PID " + p.getPid() + " swapped out to disk."
                    : "PID " + p.getPid() + " is not currently in main memory.", !ok);
        });
    }

    @FXML
    private void handleSwapIn() {
        withTypedProcess(swapPidField, memoryStatusLabel, p -> {
            boolean ok = memoryManager.swapIn(p);
            showStatus(memoryStatusLabel, ok
                    ? "PID " + p.getPid() + " swapped into main memory."
                    : "Could not swap in PID " + p.getPid() + " (not in swap, or memory full).", !ok);
        });
    }

    // ---------- Helpers ----------

    private void withSelectedOrTypedProcess(TextField pidField, Label statusLabel, Consumer<Process> action) {
        Process selected = processTable.getSelectionModel().getSelectedItem();
        if (selected != null && pidField.getText().isBlank()) {
            action.accept(selected);
            return;
        }
        withTypedProcess(pidField, statusLabel, action);
    }

    private void withTypedProcess(TextField pidField, Label statusLabel, Consumer<Process> action) {
        try {
            int pid = Integer.parseInt(pidField.getText().trim());
            Optional<Process> process = processManager.findByPid(pid);
            if (process.isPresent()) {
                action.accept(process.get());
            } else {
                showStatus(statusLabel, "No process found with PID " + pid + ".", true);
            }
        } catch (NumberFormatException e) {
            showStatus(statusLabel, "Enter a valid numeric PID.", true);
        }
    }

    private int parseIntOrDefault(String text, int defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        return Integer.parseInt(text.trim());
    }

    private void showStatus(Label label, String message, boolean isError) {
        label.setText(message);
        label.setStyle(isError ? "-fx-text-fill: #dc2626; -fx-font-style: italic;"
                : "-fx-text-fill: #2563eb; -fx-font-style: italic;");
    }
}