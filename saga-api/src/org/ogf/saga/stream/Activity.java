package org.ogf.saga.stream;

/**
 * Flags for activities of a stream. An application can poll for these
 * events or get asynchronous notification of these events by using
 * metrics.
 */
public enum Activity {
    READ (1),
    WRITE(2),
    EXCEPTION(3);

    private int value;

    Activity(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }
}
