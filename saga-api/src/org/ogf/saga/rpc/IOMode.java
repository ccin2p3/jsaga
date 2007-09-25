package org.ogf.saga.rpc;

/**
 * Describes parameter modes.
 */
public enum IOMode {
    IN (1),
    OUT (2),
    INOUT (3);

    private int value;

    IOMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
