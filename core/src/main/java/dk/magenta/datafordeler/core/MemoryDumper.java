package dk.magenta.datafordeler.core;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MemoryDumper implements Job {
    private DecimalFormat format;
    public MemoryDumper() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        format = new DecimalFormat("#,###", symbols);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(format.format(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " | " + format.format(Runtime.getRuntime().maxMemory()));
    }
}
