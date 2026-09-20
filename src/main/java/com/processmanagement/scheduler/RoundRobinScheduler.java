package com.processmanagement.scheduler;

import com.processmanagement.model.*;
import com.processmanagement.model.Process;

import java.util.*;

public class RoundRobinScheduler implements Scheduler {

    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        if (quantum <= 0) throw new IllegalArgumentException("Time quantum must be a positive integer.");
        this.quantum = quantum;
    }

    @Override
    public String getName() { return "Round Robin (quantum = " + quantum + ")"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> byArrival = new ArrayList<>(processes);
        byArrival.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));

        Map<Integer, Integer> remaining = new HashMap<>();
        Map<Integer, Integer> responseTime = new HashMap<>();
        Map<Integer, Integer> completionTime = new HashMap<>();
        for (Process p : byArrival) remaining.put(p.getPid(), p.getBurstTime());

        Deque<Process> queue = new ArrayDeque<>();
        List<GanttEntry> gantt = new ArrayList<>();

        int time = 0;
        int index = 0;
        int n = byArrival.size();

        while (index < n && byArrival.get(index).getArrivalTime() <= time) {
            queue.add(byArrival.get(index));
            index++;
        }

        while (!queue.isEmpty() || index < n) {
            if (queue.isEmpty()) {
                time = byArrival.get(index).getArrivalTime();
                while (index < n && byArrival.get(index).getArrivalTime() <= time) {
                    queue.add(byArrival.get(index));
                    index++;
                }
            }

            Process current = queue.poll();
            responseTime.putIfAbsent(current.getPid(), time - current.getArrivalTime());

            int remainingBefore = remaining.get(current.getPid());
            int slice = Math.min(quantum, remainingBefore);
            int start = time;
            int end = time + slice;

            gantt.add(new GanttEntry(current.getPid(), "P" + current.getPid(), start, end));

            time = end;
            remaining.put(current.getPid(), remainingBefore - slice);

            // Newly arrived processes join the queue before the current one goes back
            while (index < n && byArrival.get(index).getArrivalTime() <= time) {
                queue.add(byArrival.get(index));
                index++;
            }

            if (remaining.get(current.getPid()) > 0) {
                queue.add(current);
            } else {
                completionTime.put(current.getPid(), time);
            }
        }

        List<ProcessResult> results = new ArrayList<>();
        double totalWaiting = 0, totalTurnaround = 0, totalResponse = 0;

        for (Process p : byArrival) {
            int completion = completionTime.get(p.getPid());
            int turnaround = completion - p.getArrivalTime();
            int waiting = turnaround - p.getBurstTime();
            int response = responseTime.get(p.getPid());

            results.add(new ProcessResult(p.getPid(), p.getName(), p.getArrivalTime(), p.getBurstTime(),
                    p.getPriority(), completion, waiting, turnaround, response));

            totalWaiting += waiting;
            totalTurnaround += turnaround;
            totalResponse += response;
        }

        return new SchedulingResult(results, gantt,
                n == 0 ? 0 : totalWaiting / n,
                n == 0 ? 0 : totalTurnaround / n,
                n == 0 ? 0 : totalResponse / n);
    }
}