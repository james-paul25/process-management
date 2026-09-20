package src.main.java.com.processmanagement.scheduler;

import com.processmanagement.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFSScheduler implements Scheduler {

    @Override
    public String getName() { return "First Come First Served (FCFS)"; }

    @Override
    public SchedulingResult schedule(List<Process> processes) {
        List<Process> order = new ArrayList<>(processes);
        order.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));

        List<ProcessResult> results = new ArrayList<>();
        List<GanttEntry> gantt = new ArrayList<>();

        int time = 0;
        double totalWaiting = 0, totalTurnaround = 0, totalResponse = 0;

        for (Process p : order) {
            int start = Math.max(time, p.getArrivalTime());
            int completion = start + p.getBurstTime();
            int waiting = start - p.getArrivalTime();
            int turnaround = completion - p.getArrivalTime();
            int response = waiting; // runs to completion once started

            gantt.add(new GanttEntry(p.getPid(), "P" + p.getPid(), start, completion));
            results.add(new ProcessResult(p.getPid(), p.getName(), p.getArrivalTime(), p.getBurstTime(),
                    p.getPriority(), completion, waiting, turnaround, response));

            totalWaiting += waiting;
            totalTurnaround += turnaround;
            totalResponse += response;
            time = completion;
        }

        int n = order.size();
        return new SchedulingResult(results, gantt,
                n == 0 ? 0 : totalWaiting / n,
                n == 0 ? 0 : totalTurnaround / n,
                n == 0 ? 0 : totalResponse / n);
    }
}