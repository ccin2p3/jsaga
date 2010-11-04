package org.ogf.saga.task;

/**
 * This enumeration type describes the possible states of a task. Also includes
 * the states of the job class, since enumerations cannot be extended.
 */
public enum State {

    /** Describes a newly constructed task instance which has not yet run. */
    NEW(1),
    /**
     * The {@link Task#run()} method has been invoked on the task, either
     * explicitly or implicitly.
     */
    RUNNING(2),
    /** The task has finished successfully. This state is final. */
    DONE(3),
    /** 
     * The task has been cancelled, that is, {@link Task#cancel()} has been called on it.
     * This state is final.
     */
    CANCELED(4),
    /** The task has finished unsuccessfully. This state is final. */
    FAILED(5),
    /**
     * This is actually a job state, but is included here because enumerations
     * cannot be extended. It indicates that the job instance has been suspended.
     */
    SUSPENDED(6);

    private int value;

    private State(int value) {
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
