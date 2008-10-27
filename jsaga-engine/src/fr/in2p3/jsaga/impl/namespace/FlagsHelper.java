package fr.in2p3.jsaga.impl.namespace;

import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.namespace.Flags;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsHelper
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsHelper {
    private static final int ALL = Flags.ALLNAMESPACEFLAGS.or(Flags.ALLFILEFLAGS.or(JSAGAFlags.ALLFLAGS.getValue()));
    private int m_flags;

    public FlagsHelper(int flags) {
        m_flags = flags;
    }

    public void allowed(Flags... allowedFlags) throws BadParameterException {
        this.allowed(null, allowedFlags);
    }
    public void allowed(JSAGAFlags jsagaFlag, Flags... allowedFlags) throws BadParameterException {
        // set allowed flags
        int allowed = (jsagaFlag!=null ? jsagaFlag.getValue() : Flags.NONE.getValue());
        for (Flags flag : allowedFlags) {
            allowed = flag.or(allowed);
        }

        // set forbidden flags
        int forbidden = ALL - allowed;

        // set bad flags
        int bad = m_flags & forbidden;
        if (bad > 0) {
            throw new BadParameterException("Flags not allowed for this method: "+bad);
        }
    }

    public void required(Flags... requiredFlags) throws BadParameterException {
        // set required flags
        int required = Flags.NONE.getValue();
        for (Flags flag : requiredFlags) {
            required = flag.or(required);
        }

        // set unset flags
        int unset = ALL - m_flags;

        // set missing flags
        int missing = unset & required;
        if (missing > 0) {
            throw new BadParameterException("Flags missing for this method: "+missing);
        }
    }

    public int add(Flags... addedFlags) {
        return this.add(null, addedFlags);
    }
    public int add(JSAGAFlags jsagaFlag, Flags... addedFlags) {
        int addition = m_flags;
        if (jsagaFlag != null) {
            addition = jsagaFlag.or(addition);
        }
        for (Flags flag : addedFlags) {
            addition = flag.or(addition);
        }
        return addition;
    }

    public int substract(Flags... substractedFlags) {
        return this.substract(null, substractedFlags);
    }
    public int substract(JSAGAFlags jsagaFlag, Flags... substractedFlags) {
        int substraction = m_flags;
        if (jsagaFlag != null) {
            if (jsagaFlag.isSet(substraction)) {
                substraction -= jsagaFlag.getValue();
            }
        }
        for (Flags flag : substractedFlags) {
            if (flag.isSet(substraction)) {
                substraction -= flag.getValue();
            }
        }
        return substraction;
    }
}
