package org.ogf.saga.task;

/**
 * When waiting for tasks in a task container, the user can either wait for all
 * tasks in the container, or for any task in the container.
 */
public enum WaitMode {
    /**
     *  Wait for all tasks in the container. {@link Task#waitFor()} only returns
     *  when all tasks in the container have reached a final state.
     */
    ALL(0),
    /**
     *  Wait for any task in the container. {@link Task#waitFor()} when one or more
     * tasks in the container have reached a final state.
     */
    ANY(1);

    private int value;

    WaitMode(int value) {
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
