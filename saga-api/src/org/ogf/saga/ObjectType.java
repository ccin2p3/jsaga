package org.ogf.saga;

/**
 * Enumerates the different object types in SAGA.
 */
public enum ObjectType {

    UNKNOWN (-1),
    EXCEPTION (1),
    SESSION (2),
    CONTEXT (3),
    TASK (4),
    TASKCONTAINER (5),
    BUFFER (6),
    METRIC (7),
    NSENTRY (8),
    NSDIRECTORY (9),
    IOVEC (10),
    FILE (11),
    DIRECTORY (12),
    LOGICALFILE (13),
    LOGICALDIRECTORY (14),
    JOBDESCRIPTION (15),
    JOBSERVER (16),
    JOB (17),
    STREAMSERVER (18),
    STREAM (19),
    RPC (20),
    PARAMETER (21);


    private int value;

    ObjectType(int value) {
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
