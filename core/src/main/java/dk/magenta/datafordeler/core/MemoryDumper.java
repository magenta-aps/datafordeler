package dk.magenta.datafordeler.core;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MemoryDumper implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " | " + Runtime.getRuntime().maxMemory());
    }
}
