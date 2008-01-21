package org.ogf.saga.namespace;

/**
 * Enumerates some flags for methods in this package.
 * Note: since enumerations cannot be extended, all flags are included here.
 * The SAGA specs gets away with this as it has no real enumeration type.
 * In Java, the values should all be of the same type, or else the file package
 * for instance cannot inherit from the namespace package.
 * These flags are meant to be or-ed together, resulting in an integer.
 * Java does not define arithmetic operators for enumerations,
 * so methods are added here to test for presence and or-ing.
 * For instance,
 * <br>
 * <code>
 * int flags = Flags.EXCL.or(Flags.READ.or(Flags.WRITE));
 * <br>
 * if (Flags.READ.isSet(flags)) ...
 * </code>
 */
public enum Flags {

    NONE (0),
    OVERWRITE (1),
    RECURSIVE (2),
    DEREFERENCE (4),
    CREATE (8),
    EXCL (16),
    LOCK (32),
    CREATEPARENTS (64),
    ALLNAMESPACEFLAGS(1|2|4|8|16|32|64),
    TRUNCATE(128),
    APPEND(256),
    READ(512),
    WRITE(1024),
    READWRITE(512|1024),
    BINARY(2048),
    ALLLOGICALFILEFLAGS(512|1024),
    ALLFILEFLAGS(128|256|512|1024|2048);

    private int value;

    Flags(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the result of or-ing this flag into an integer.
     * @param val the value to OR this enumeration value into.
     * @return the result of or-ing this flag into the integer parameter.
     */
    public int or(int val) {
        return val | value;
    }

    /**
     * Returns the result of or-ing this flag into another.
     * @param val the value to OR this enumeration value into.
     * @return the result of or-ing this flag into the integer parameter.
     */
    public int or(Flags val) {
        return val.value | value;
    }

    /**
     * Tests for the presence of this flag in the specified value.
     * @param val the value.
     * @return <code>true</code> if this flag is present.
     */
    public boolean isSet(int val) {
        if (value == val) {
            // Also tests for 0 (NONE) which is assumed to be set only when
            // no other values are set.
            return true;
        }
        return (val & value) != 0;
    }
}
