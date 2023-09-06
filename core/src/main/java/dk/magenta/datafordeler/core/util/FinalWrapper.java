package dk.magenta.datafordeler.core.util;

/**
 * Helper method for variables that need to be declared final due to nested methods (e.g. lambdas)
 * Usage:
 * final FinalWrapper<Boolean> f = new FinalWrapper<>(false);
 * stream.foreach(v -> {
 * ...
 * boolean b = f.getInner()
 * ...
 * f.setInner(new Boolean(true));
 * ...
 * });
 */
public class FinalWrapper<I> {
    I inner;

    public FinalWrapper(I inner) {
        this.inner = inner;
    }

    public I getInner() {
        return this.inner;
    }

    public void setInner(I inner) {
        this.inner = inner;
    }
}
