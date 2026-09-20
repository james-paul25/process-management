package src.main.java.com.processmanagement.scheduler;

import com.processmanagement.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Non-preemptive Shortest Job First. */
public class SJFScheduler implements Scheduler {

    @Override
    public String getName() { return "Shortest Job First (SJF, non-preemptive)"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> pending = new ArrayList<>(processes);
        List<ProcessResult> results = new ArrayList<>();
        List<GanttEntry> gantt = new ArrayList<>();

        int time = 0;
        double totalWaiting = 0, totalTurnaround = 0, totalResponse = 0;
        int n = pending.size();

        while (!pending.isEmpty()) {
            final int currentTime = time;
            List<Process> arrived = pending.stream()
                    .filter(p -> p.getArrivalTime() <= currentTime)
                    .toList();

            if (arrived.isEmpty()) {
                time = pending.stream().mapToInt(Process::getArrivalTime).min().orElse(time);
                continue;
            }

            Process next = arrived.stream()
                    .min(Comparator.comparingInt(Process::getBurstTime)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparingInt(Process::getPid))
                    .orElseThrow();

            int start = Math.max(time, next.getArrivalTime());
            int completion = start + next.getBurstTime();
            int waiting = start - next.getArrivalTime();
            int turnaround = completion - next.getArrivalTime();
            int response = waiting;

            gantt.add(new GanttEntry(next.getPid(), "P" + next.getPid(), start, completion));
            results.add(new ProcessResult(next.getPid(), next.getName(), next.getArrivalTime(), next.getBurstTime(),
                    next.getPriority(), completion, waiting, turnaround, response));

            totalWaiting += waiting;
            totalTurnaround += turnaround;
            totalResponse += response;

            time = completion;
            pending.remove(next);
        }

        return new SchedulingResult(results, gantt,
                n == 0 ? 0 : totalWaiting / n,
                n == 0 ? 0 : totalTurnaround / n,
                n == 0 ? 0 : totalResponse / n);
    }
}