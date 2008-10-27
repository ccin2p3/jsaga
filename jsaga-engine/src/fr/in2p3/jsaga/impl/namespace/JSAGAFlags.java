package fr.in2p3.jsaga.impl.namespace;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSAGAFlags
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public enum JSAGAFlags {
    /** Bypass existence check */
    BYPASSEXIST(4096),
    /** Preserve times */
    PRESERVETIMES(8192),
    /** All flags */
    ALLFLAGS(4096 | 8192);

    private int value;

    JSAGAFlags(int value) {
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
