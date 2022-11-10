package dk.magenta.datafordeler.core.util;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class LabeledSequenceInputStream extends InputStream {

    private final Enumeration<Pair<String, InputStream>> e;
    Pair<String, InputStream> current;
    private InputStream in;


    public LabeledSequenceInputStream(List<Pair<String, InputStream>> e) {
        this(new Vector<>(e).elements());
    }

    public LabeledSequenceInputStream(Enumeration<Pair<String, InputStream>> e) {
        this.e = e;
        try {
            nextStream();
        } catch (IOException ex) {
            // This should never happen
            throw new Error("panic");
        }
    }

    public LabeledSequenceInputStream(String label1, InputStream s1, String label2, InputStream s2) {
        Vector<Pair<String, InputStream>> v = new Vector<>();
        v.add(Pair.of(label1, s1));
        v.add(Pair.of(label2, s2));
        this.e = v.elements();
        try {
            nextStream();
        } catch (IOException ex) {
            // This should never happen
            throw new Error("panic");
        }
    }

    public String getCurrentLabel() {
        return this.current.getLeft();
    }

    /**
     * Continues reading in the next stream if an EOF is reached.
     */
    private final void nextStream() throws IOException {
        if (in != null) {
            in.close();
        }

        if (this.e.hasMoreElements()) {
            this.current = e.nextElement();
            this.in = this.current.getRight();
            if (in == null) {
                throw new NullPointerException();
            }
        } else in = null;

    }

    public int available() throws IOException {
        if (in == null) {
            return 0; // no way to signal EOF from available()
        }
        return in.available();
    }

    public int read() throws IOException {
        while (in != null) {
            int c = in.read();
            if (c != -1) {
                return c;
            }
            nextStream();
        }
        return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (in == null) {
            return -1;
        } else if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        do {
            int n = in.read(b, off, len);
            if (n > 0) {
                return n;
            }
            nextStream();
        } while (in != null);
        return -1;
    }

    public void close() throws IOException {
        do {
            nextStream();
        } while (in != null);
    }
}
