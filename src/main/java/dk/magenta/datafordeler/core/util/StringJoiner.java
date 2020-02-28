package dk.magenta.datafordeler.core.util;

import java.util.Collection;

public class StringJoiner {

    java.util.StringJoiner inner;

    public StringJoiner(CharSequence delimiter) {
        this.inner = new java.util.StringJoiner(delimiter);
    }

    public StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        this.inner = new java.util.StringJoiner(delimiter, prefix, suffix);
    }

    public StringJoiner add(CharSequence newElement) {
        this.inner.add(newElement);
        return this;
    }

    public int length() {
        return this.inner.length();
    }

    public StringJoiner merge(StringJoiner other) {
        this.inner.merge(other.inner);
        return this;
    }

    public StringJoiner merge(java.util.StringJoiner other) {
        this.inner.merge(other);
        return this;
    }

    public StringJoiner setEmptyValue(CharSequence emptyValue) {
        this.inner.setEmptyValue(emptyValue);
        return this;
    }

    public String toString() {
        return this.inner.toString();
    }

    //--------------------------------------------------------------------------
    // New methods

    public <T extends CharSequence> StringJoiner addAll(Collection<T> newElements) {
        for (CharSequence newElement : newElements) {
            this.inner.add(newElement);
        }
        return this;
    }
}
