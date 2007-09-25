package org.ogf.saga.namespace;

/**
 * Enumerates some flags for methods in this package.
 * Note: since enumerations cannot be extended, all flags are included here.
 * The SAGA specs gets away with this as it has no real enumeration type.
 * In Java, the values should all be of the same type, or else the file package
 * for instance cannot inherit from the namespace package.
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
    TRUNCATE(128),
    APPEND(256),
    READ(512),
    WRITE(1024),
    READWRITE(1536),
    BINARY(2048);


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
}
