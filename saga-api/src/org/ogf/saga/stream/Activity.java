package org.ogf.saga.stream;

/**
 * Flags for activities of a stream. An application can poll for these
 * events or get asynchronous notification of these events by using
 * metrics.
 * These flags are meant to be or-ed together, resulting in an integer,
 * so methods are added to test for presence and or-ing.
 */
public enum Activity {
    READ (1),
    WRITE(2),
    EXCEPTION(4);

    private int value;

    Activity(int value) {
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
    public int or(Activity val) {
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
