package com.mq;

import java.util.Objects;

public class CacheEntry {
    private String text;
    private int sequenceNr;

    public String getText() {
        return text;
    }

    public int getSequenceNr() {
        return sequenceNr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheEntry that = (CacheEntry) o;

        if (sequenceNr != that.sequenceNr) return false;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + sequenceNr;
        return result;
    }

    public CacheEntry(String text, int sequenceNr) {
        this.text = text;
        this.sequenceNr = sequenceNr;
    }


}
