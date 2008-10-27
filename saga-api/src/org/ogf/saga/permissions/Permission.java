package org.ogf.saga.permissions;

/**
 * Enumerates all permission values. These flags are meant to be or-ed together,
 * resulting in an integer, so methods are added to test for presence and
 * or-ing.
 */
public enum Permission {
    NONE(0), QUERY(1), READ(2), WRITE(4), EXEC(8), OWNER(16),
        ALL(31);

    private int value;

    Permission(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this permission.
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
    public int or(Permission val) {
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
