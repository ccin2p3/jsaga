package org.ogf.saga.file;

/**
 * Determines the seekmode for {@link File#seek}: the specified offset is
 * interpreted with respect to the start, the current position, or the end.
 */
public enum SeekMode {
    /** Seek from the start of the file. */
    START(1), 
    /** Seek from the current position in the file. */
    CURRENT(2),
    /** Seek from the end of the file. */
    END(3);

    private int value;

    SeekMode(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * 
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }
}
