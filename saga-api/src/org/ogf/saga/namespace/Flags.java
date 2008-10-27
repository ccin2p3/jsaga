package org.ogf.saga.namespace;

/**
 * Enumerates some flags for methods in this package. Note: since enumerations
 * cannot be extended, all flags are included here. The SAGA specs gets away
 * with this as it has no real enumeration type. In Java, the values should all
 * be of the same type, or else the file package for instance cannot inherit
 * from the namespace package. These flags are meant to be or-ed together,
 * resulting in an integer. Java does not define arithmetic operators for
 * enumerations, so methods are added here to test for presence and or-ing. For
 * instance, <br>
 * <code>
 * int flags = Flags.EXCL.or(Flags.READ.or(Flags.WRITE));
 * <br>
 * if (Flags.READ.isSet(flags)) ...
 * </code>
 */
public enum Flags {
    /** Indicates the absence of flags. */
    NONE(0),
    /**
     * Enforces an operation which creates a new namespace entry to continue
     * even if the target entry does not already exist.
     */
    OVERWRITE(1),
    /** Enforces an operation to apply recursively on a directory tree. */
    RECURSIVE(2),
    /**
     * Enforces an operation to apply not to the entry pointed to by the target name,
     * but to the link target of that entry.
     */
    DEREFERENCE(4),
    /** 
     * Allows a namespace entry to be created while opening it, if it does not
     * already exist.
     */
    CREATE(8),
    /** 
     * If the entry already exists, the {@link #CREATE} flag is not silenty
     * ignored. Instead, an {@link org.ogf.saga.error.AlreadyExistsException AlreadyExistsException}
     * is raised.
     */
    EXCL(16),
    /** Enforces a lock on the namespace entry when it is opened. */
    LOCK(32), 
    /** Implies that missing path elements are created on the fly. */
    CREATEPARENTS(64),
    /** All flags applicable to the namespace package. */
    ALLNAMESPACEFLAGS(1 | 2 | 4 | 8 | 16 | 32 | 64),
    /** Upon opening, the file is truncated to length 0. */
    TRUNCATE(128),
    /** Upon opening, the file pointer is set to the end of the file. */
    APPEND(256),
    /** The file or directory is opened for reading. */
    READ(512),
    /** The file or directory is opened for writing. */
    WRITE(1024),
    /** The file or directory is opened for reading and writing. */
    READWRITE(512 | 1024),
    /** For OS-es that distinguish between binary and non-binary modes. */
    BINARY(2048),
    /** All flags applicable to the logical file package. */
    ALLLOGICALFILEFLAGS(512 | 1024),
    /** All flags applicable to the file package. */
    ALLFILEFLAGS(128 | 256 | 512 | 1024 | 2048);

    private int value;

    Flags(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * 
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the result of or-ing this flag into an integer.
     * 
     * @param val
     *            the value to OR this enumeration value into.
     * @return the result of or-ing this flag into the integer parameter.
     */
    public int or(int val) {
        return val | value;
    }

    /**
     * Returns the result of or-ing this flag into another.
     * 
     * @param val
     *            the value to OR this enumeration value into.
     * @return the result of or-ing this flag into the integer parameter.
     */
    public int or(Flags val) {
        return val.value | value;
    }

    /**
     * Tests for the presence of this flag in the specified value.
     * 
     * @param val
     *            the value.
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
