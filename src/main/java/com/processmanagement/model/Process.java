package com.processmanagement.model;

import javafx.beans.property.*;

/**
 * Represents a SIMULATED process used to demonstrate operating-system
 * concepts. It is NOT itself an operating-system process. When this
 * process is executed via "Execute (Real OS Process)", the real OS PID
 * assigned by the operating system is stored separately in {@code osPid}.
 */
public class Process {

    private final IntegerProperty pid;
    private final StringProperty name;
    private final ObjectProperty<ProcessState> state;
    private final IntegerProperty arrivalTime;
    private final IntegerProperty burstTime;
    private final IntegerProperty remainingTime;
    private final IntegerProperty priority;
    private final LongProperty osPid; // -1 means "not backed by a real OS process"

    public Process(int pid, String name, int arrivalTime, int burstTime, int priority) {
        this.pid = new SimpleIntegerProperty(pid);
        this.name = new SimpleStringProperty(name);
        this.state = new SimpleObjectProperty<>(ProcessState.NEW);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.remainingTime = new SimpleIntegerProperty(burstTime);
        this.priority = new SimpleIntegerProperty(priority);
        this.osPid = new SimpleLongProperty(-1);
    }

    public int getPid() { return pid.get(); }
    public IntegerProperty pidProperty() { return pid; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public ProcessState getState() { return state.get(); }
    public void setState(ProcessState value) { state.set(value); }
    public ObjectProperty<ProcessState> stateProperty() { return state; }

    public int getArrivalTime() { return arrivalTime.get(); }
    public IntegerProperty arrivalTimeProperty() { return arrivalTime; }

    public int getBurstTime() { return burstTime.get(); }
    public IntegerProperty burstTimeProperty() { return burstTime; }

    public int getRemainingTime() { return remainingTime.get(); }
    public void setRemainingTime(int value) { remainingTime.set(value); }
    public IntegerProperty remainingTimeProperty() { return remainingTime; }

    public int getPriority() { return priority.get(); }
    public IntegerProperty priorityProperty() { return priority; }

    public long getOsPid() { return osPid.get(); }
    public void setOsPid(long value) { osPid.set(value); }
    public LongProperty osPidProperty() { return osPid; }

    @Override
    public String toString() {
        return "PID " + getPid() + " - " + getName() + " [" + getState() + "]";
    }
}