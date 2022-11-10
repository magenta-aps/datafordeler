package dk.magenta.datafordeler.core;

import dk.magenta.datafordeler.core.command.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;

public abstract class AbstractTask<W extends Worker> implements
        InterruptableJob {

    public static final String DATA_DUMMYRUN = "dummyRun";

    private static final Logger log = LogManager.getLogger(AbstractTask.class.getCanonicalName());

    private W worker;

    public AbstractTask() {
    }

    @Override
    public void execute(JobExecutionContext executionContext) {
        JobDataMap dataMap = executionContext.getMergedJobDataMap();
        boolean dummyRun = dataMap.getBoolean(DATA_DUMMYRUN);
        if (!dummyRun) {
            Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    ex.printStackTrace();
                }
            };
            this.worker = createWorker(dataMap);
            this.worker.setUncaughtExceptionHandler(exceptionHandler);
            this.worker.start();
        }
    }

    protected abstract W createWorker(JobDataMap dataMap);

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.worker.interrupt();
        try {
            this.worker.join();
        } catch (InterruptedException e) {
            throw new UnableToInterruptJobException(e);
        }
    }
}
