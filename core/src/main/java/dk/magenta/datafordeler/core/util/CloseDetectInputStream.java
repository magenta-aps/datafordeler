package dk.magenta.datafordeler.core.util;

import dk.magenta.datafordeler.core.io.WrappedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * InputStream that can execute callbacks when it is closed.
 */
public class CloseDetectInputStream extends WrappedInputStream {

    private final List<Runnable> beforeCloseListeners = new ArrayList<>();
    private final List<Runnable> afterCloseListeners = new ArrayList<>();

    public CloseDetectInputStream(InputStream in) {
        super(in);
    }

    /**
     * Add a callback to be run when the close() method is called, before the
     * wrapped stream close() method is called
     */
    public void addBeforeCloseListener(Runnable closeListener) {
        this.beforeCloseListeners.add(closeListener);
    }

    /**
     * Add a callback to be run when the close() method is called, after the
     * wrapped stream close() method is called
     */
    public void addAfterCloseListener(Runnable closeListener) {
        this.afterCloseListeners.add(closeListener);
    }

    /**
     * Run registered callbacks before and after calling the wrapped stream close()
     */
    @Override
    public void close() throws IOException {
        this.runListeners(this.beforeCloseListeners);
        super.close();
        this.runListeners(this.afterCloseListeners);
    }

    private void runListeners(List<Runnable> listeners) {
        for (Runnable closeListener : listeners) {
            try {
                closeListener.run();
            } catch (Exception e) {
            }
        }
    }

}
