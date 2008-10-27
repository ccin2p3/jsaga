package org.ogf.saga.rpc;

/**
 * Describes parameter modes.
 */
public enum IOMode {
    /** The parameter is an input parameter. */
    IN(1),
    /** The parameter is an output parameter. */
    OUT(2),
    /** The parameter is an input and output parameter. */
    INOUT(3);

    private int value;

    IOMode(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value of this enumeration literal.
     * 
     * @return the value.
     */
    public int getValue() {
        return this.value;
    }
}
