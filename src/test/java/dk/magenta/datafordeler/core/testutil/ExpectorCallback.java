package dk.magenta.datafordeler.core.testutil;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExpectorCallback implements Callback, Future<Boolean> {

    private boolean found = false;
    private boolean cancelled = false;
    private boolean done = false;

    public ExpectorCallback() {
    }

    @Override
    public void run(String key, Map<String, String[]> params) {
        this.found = true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.cancelled = true;
        return this.done;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        try {
            return this.get(1, TimeUnit.HOURS);
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long millis = unit.toMillis(timeout);
        long step = 100;
        for (int i = 0; i < millis && !this.cancelled && !this.found; i += step) {
            try {
                Thread.sleep(step);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.done = true;
        return this.found;
    }
}
