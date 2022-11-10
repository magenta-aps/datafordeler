package dk.magenta.datafordeler.core.command;

/**
 * Command job superclass. Subclasses execute jobs in the run() method,
 * but should respect the setting of doCancel, which means to stop the run
 */
public abstract class Worker extends Thread implements Runnable {

    public static class WorkerCallback {
        public void onComplete(boolean cancelled) {
        }

        public void onError(Throwable e) {
        }
    }

    protected boolean doCancel = false;
    protected WorkerCallback callback = null;

    public void end() {
        this.doCancel = true;
        this.interrupt();
    }

    public void setCallback(WorkerCallback callback) {
        this.callback = callback;
    }

    protected void onComplete() {
        if (this.callback != null) {
            try {
                this.callback.onComplete(this.doCancel);
            } catch (Exception ex) {
            }
        }
    }

    protected void onError(Throwable e) {
        if (this.callback != null) {
            try {
                this.callback.onError(e);
            } catch (Exception ex) {
            }
        }
    }

    public boolean isDoCancel() {
        return doCancel;
    }

    public abstract void run();
}
