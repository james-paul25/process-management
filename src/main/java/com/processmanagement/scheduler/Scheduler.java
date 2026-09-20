package src.main.java.com.processmanagement.scheduler;

import com.processmanagement.model.Process;
import com.processmanagement.model.SchedulingResult;

import java.util.List;

public interface Scheduler {
    SchedulingResult schedule(List<Process> processes);
    String getName();
}