package com.processmanagement.service;

import com.processmanagement.model.Process;
import com.processmanagement.model.ProcessState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * SIMULATION of main memory vs. swap/disk. This does not touch the
 * computer's actual virtual memory or swap file — it only models the
 * concept for demonstration purposes.
 */
public class MemoryManager {

    private final int capacity;
    private final ObservableList<Process> mainMemory = FXCollections.observableArrayList();
    private final ObservableList<Process> swapSpace = FXCollections.observableArrayList();

    public MemoryManager(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }
    public ObservableList<Process> getMainMemory() { return mainMemory; }
    public ObservableList<Process> getSwapSpace() { return swapSpace; }

    public boolean loadToMemory(Process p) {
        if (mainMemory.contains(p) || mainMemory.size() >= capacity) return false;
        swapSpace.remove(p);
        mainMemory.add(p);
        if (p.getState() != ProcessState.TERMINATED) {
            p.setState(ProcessState.READY);
        }
        return true;
    }

    public boolean swapOut(Process p) {
        if (!mainMemory.contains(p)) return false;
        mainMemory.remove(p);
        swapSpace.add(p);
        p.setState(ProcessState.SWAPPED_OUT);
        return true;
    }

    public boolean swapIn(Process p) {
        if (!swapSpace.contains(p) || mainMemory.size() >= capacity) return false;
        swapSpace.remove(p);
        mainMemory.add(p);
        p.setState(ProcessState.READY);
        return true;
    }
}