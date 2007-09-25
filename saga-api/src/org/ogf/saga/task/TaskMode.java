package org.ogf.saga.task;

/**
 * This enumeration type describes the possible ways to create a task:
 * Asynchronous (the task is started in RUNNING state), Task (the task is
 * started in NEW state), or Synchronous (the task is started and waited for).
 */
public enum TaskMode {

    ASYNC (1),
    TASK (2),
    SYNC (3);

    private int value;

    TaskMode (int value) {
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
