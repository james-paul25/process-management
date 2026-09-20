package com.processmanagement.model;

import java.util.List;

/** Full output of running a scheduling algorithm over a set of processes. */
public class SchedulingResult {
    private final List<ProcessResult> results;
    private final List<GanttEntry> ganttEntries;
    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;

    public SchedulingResult(List<ProcessResult> results, List<GanttEntry> ganttEntries,
                             double averageWaitingTime, double averageTurnaroundTime,
                             double averageResponseTime) {
        this.results = results;
        this.ganttEntries = ganttEntries;
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageResponseTime = averageResponseTime;
    }

    public List<ProcessResult> getResults() { return results; }
    public List<GanttEntry> getGanttEntries() { return ganttEntries; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public double getAverageResponseTime() { return averageResponseTime; }
}