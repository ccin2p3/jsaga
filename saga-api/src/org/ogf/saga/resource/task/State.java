package org.ogf.saga.resource.task;

public enum State {
    /**
     * wut?
     */
    UNKNOWN(0),

    /**
     * will become active eventually
     */
    NEW(1),

    /**
     * will become active eventually
     */
    PENDING(2),

    /**
     * accepting jobs, jobs can run
     */
    ACTIVE(4),

    /**
     * jobs still run, not accepting new jobs
     */
    DRAINING(8),

    /**
     * Pending | Active | Draining
     */
    RUNNING(14),

    /**
     * closed by user
     */
    CLOSED(16),

    /**
     * closed by system
     */
    EXPIRED(32),

    /**
     * closed unexpectedly by system or internally
     */
    FAILED(64),

    /**
     * Closed | Expired | Failed
     */
    FINAL(112);

    private int value;

    State(int value) {
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

    public boolean isRunning() {
        return (State.RUNNING.value & this.value) > 0;
    }
    public boolean isFinal() {
        return (State.FINAL.value & this.value) > 0;
    }
}
