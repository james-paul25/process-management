package src.main.java.com.processmanagement.model;

/** One block in a Gantt chart: process PID running from start to end. */
public class GanttEntry {
    private final int pid;
    private final String label;
    private final int start;
    private final int end;

    public GanttEntry(int pid, String label, int start, int end) {
        this.pid = pid;
        this.label = label;
        this.start = start;
        this.end = end;
    }

    public int getPid() { return pid; }
    public String getLabel() { return label; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public int getDuration() { return end - start; }
}