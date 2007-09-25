package org.ogf.saga.file;

/**
 * Determines the seekmode for {@link File#seek}:
 * seek from start, current position, or end.
 */
public enum SeekMode {
    START(1),
    CURRENT(2),
    END(3);

    private int value;

    SeekMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
