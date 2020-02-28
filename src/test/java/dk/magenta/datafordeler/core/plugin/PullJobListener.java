package dk.magenta.datafordeler.core.plugin;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PullJobListener implements JobListener {

    private String name;

    public enum Event {
        jobToBeExecuted,
        jobExecutionVetoed,
        jobWasExecuted
    }

    private HashMap<Event, ArrayList<JobExecutionContext>> contextList;

    public PullJobListener(String name) {
        this.name = name;
        this.contextList = new HashMap<>();
        this.contextList.put(Event.jobToBeExecuted, new ArrayList<>());
        this.contextList.put(Event.jobExecutionVetoed, new ArrayList<>());
        this.contextList.put(Event.jobWasExecuted, new ArrayList<>());
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void reset(Event event) {
        this.contextList.get(event).clear();
    }

    public int size(Event event) {
        return this.contextList.get(event).size();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        this.contextList.get(Event.jobToBeExecuted).add(jobExecutionContext);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        this.contextList.get(Event.jobExecutionVetoed).add(jobExecutionContext);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        this.contextList.get(Event.jobWasExecuted).add(jobExecutionContext);
    }
}
