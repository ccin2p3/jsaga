package org.ogf.saga.stream;

/**
 * SAGA Stream states.
 * Newly created streams start in the NEW state. A {@link Stream#connect()}
 * call brings it either in state OPEN or ERROR. From the OPEN state, when an
 * error occurs it goes to state ERROR. When the remote party closes the
 * connection it goes into state DROPPED, and after a {@link Stream#close()}
 * call it goes into state CLOSED.
 * CLOSED, DROPPED, and ERROR are final states: I/O is no longer possible.
 */
public enum StreamState {
    NEW (1),
    OPEN (2),
    CLOSED (3),
    DROPPED (4),
    ERROR (5);

    private int value;

    StreamState(int value) {
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
