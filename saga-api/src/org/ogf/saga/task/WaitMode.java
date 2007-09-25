package org.ogf.saga.task;

/**
 * When waiting for tasks in a task container, the user can either wait
 * for all tasks in the container, or for any task in the container.
 */
public enum WaitMode {
    ALL (0),
    ANY (1);

    private int value;

    WaitMode(int value) {
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
