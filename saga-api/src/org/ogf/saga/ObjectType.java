package org.ogf.saga;

/**
 * Enumerates the different object types in SAGA.
 */
public enum ObjectType {

    UNKNOWN (-1),
    EXCEPTION (1),
    URL(2),
    BUFFER(3),
    SESSION (4),
    CONTEXT (5),
    TASK (6),
    TASKCONTAINER (7),
    METRIC (8),
    NSENTRY (9),
    NSDIRECTORY (10),
    IOVEC (11),
    FILE (12),
    DIRECTORY (13),
    LOGICALFILE (14),
    LOGICALDIRECTORY (15),
    JOBDESCRIPTION (16),
    JOBSERVICE (17),
    JOB (18),
    JOBSELF (19),
    STREAMSERVICE (20),
    STREAM (21),
    PARAMETER (22),
    RPC (23),
    
    // Added object type for Java bindings.
    FILEINPUTSTREAM(24),
    FILEOUTPUTSTREAM(25);


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
