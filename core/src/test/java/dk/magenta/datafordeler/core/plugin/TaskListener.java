package dk.magenta.datafordeler.core.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.*;
import java.util.stream.Collectors;

public class TaskListener implements JobListener {

    private static Logger log = LogManager.getLogger(TaskListener.class.getCanonicalName());
    private String name;

    public enum Event {
        jobToBeExecuted,
        jobExecutionVetoed,
        jobWasExecuted
    }

    private Map<Event, ArrayList<JobExecutionContext>> contextList;

    private List<JobExecutionContext> getContext(Event key) {
        return this.contextList.computeIfAbsent(key,
            (k) -> new ArrayList<>());
    }

    public TaskListener(String name) {
        this.name = name;
        this.contextList = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void reset() {
        this.contextList.clear();
    }

    public int size(Event event) {
        log.info(
            this.contextList.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList())
        );

        return this.getContext(event).size();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        log.info("to be {}", jobExecutionContext.getTrigger());
        this.getContext(Event.jobToBeExecuted).add(jobExecutionContext);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.info("veto {}", jobExecutionContext.getTrigger());
        this.getContext(Event.jobExecutionVetoed).add(jobExecutionContext);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        log.info("was {}", jobExecutionContext.getTrigger());
        this.getContext(Event.jobWasExecuted).add(jobExecutionContext);
    }
}
