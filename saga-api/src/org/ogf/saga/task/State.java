package org.ogf.saga.task;

/**
 * This enumeration type describes the possible states of a task.
 * Also includes the states of the job class, since enumerations cannot
 * be extended.
 */
public enum State {

    NEW (1),
    RUNNING (2),
    DONE (3),
    CANCELED (4),
    FAILED (5),
    SUSPENDED(6);

    private int value;

    State (int value) {
        this.value = value; 
    }

    /**
     * Returns the integer value of this enumeration literal.
     * @return the value.
     */
    public int getValue() {
        return value;
    }
}
