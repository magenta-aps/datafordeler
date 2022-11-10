package dk.magenta.datafordeler.core.io;

import java.io.IOException;
import java.io.InputStream;

public class WrappedInputStream extends InputStream {

    private final InputStream inner;

    public WrappedInputStream(InputStream inner) {
        this.inner = inner;
    }

    public InputStream getInner() {
        return this.inner;
    }

    @Override
    public int read() throws IOException {
        return this.inner.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.inner.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.inner.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return this.inner.skip(n);
    }

    @Override
    public int available() throws IOException {
        return this.inner.available();
    }

    @Override
    public void close() throws IOException {
        this.inner.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.inner.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.inner.reset();
    }

    @Override
    public boolean markSupported() {
        return this.inner.markSupported();
    }
}
