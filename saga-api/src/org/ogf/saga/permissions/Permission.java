package org.ogf.saga.permissions;

/**
 * Enumerates all permission values.
 */
public enum Permission {
    UNKNOWN(-1),
    NONE(0),
    QUERY(1),
    READ(2),
    WRITE(4),
    EXEC(8),
    OWNER(16),
    ALL(31);

    private int value;

    Permission(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this permission.
     * @return the integer value.
     */
    public int getValue() {
        return value;
    }
}
