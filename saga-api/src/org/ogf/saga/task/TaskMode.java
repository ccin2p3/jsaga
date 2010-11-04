package org.ogf.saga.task;

/**
 * This enumeration type describes the possible ways to create a task:
 * Asynchronous (the task is started in RUNNING state), Task (the task is
 * started in NEW state), or Synchronous (the task is started and waited for).
 */
public enum TaskMode {

    /** 
     * The task is started in {@link State#RUNNING} state, that is, the {@link Task#run()} call
     * is implicit.
     */
    ASYNC(1),
    /** The task is started in {@link State#NEW} state. */
    TASK(2),
    /** The task is started in {@link State#RUNNING} state, and is waited for. */
    SYNC(3);

    private int value;

    private TaskMode(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * 
     * @return the value.
     */
    public int getValue() {
        return value;
    }
}
