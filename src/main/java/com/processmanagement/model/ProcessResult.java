package com.processmanagement.model;

/** Immutable result of a scheduling run for a single process. */
public class ProcessResult {
    private final int pid;
    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private final int completionTime;
    private final int waitingTime;
    private final int turnaroundTime;
    private final int responseTime;

    public ProcessResult(int pid, String name, int arrivalTime, int burstTime, int priority,
                          int completionTime, int waitingTime, int turnaroundTime, int responseTime) {
        this.pid = pid;
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.completionTime = completionTime;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
        this.responseTime = responseTime;
    }

    public int getPid() { return pid; }
    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getCompletionTime() { return completionTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getResponseTime() { return responseTime; }
}