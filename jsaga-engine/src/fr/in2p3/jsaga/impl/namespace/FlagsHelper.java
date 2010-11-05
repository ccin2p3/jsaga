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
    private static final int ALL = Flags.ALLFILEFLAGS.or(JSAGAFlags.ALLFLAGS.getValue());
    private int m_flags;

    public FlagsHelper(int flags) {
        m_flags = flags;
    }

    public void allowed(Flags... allowedFlags) throws BadParameterException {
        this.allowed(null, allowedFlags);
    }
    public void allowed(JSAGAFlags jsagaFlag, Flags... allowedFlags) throws BadParameterException {
        // set allowed flags
        int allowed = toInt(jsagaFlag, allowedFlags);

        // set forbidden flags
        int forbidden = ALL - allowed;

        // set bad flags
        int bad = m_flags & forbidden;
        if (bad > 0) {
            throw new BadParameterException("Flags not allowed for this method: "+bad);
        }
    }

    public void required(Flags... requiredFlags) throws BadParameterException {
        this.required(null, requiredFlags);
    }
    public void required(JSAGAFlags jsagaFlag, Flags... requiredFlags) throws BadParameterException {
        // set required flags
        int required = toInt(jsagaFlag, requiredFlags);

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
        // set added flags
        int added = toInt(jsagaFlag, addedFlags);
        
        // return addition
        return m_flags | added;
    }

    public int keep(Flags... keepedFlags) {
        return this.keep(null, keepedFlags);
    }
    public int keep(JSAGAFlags jsagaFlag, Flags... keepedFlags) {
        // set keeped flags
        int keeped = toInt(jsagaFlag, keepedFlags);

        // return intersection
        return m_flags & keeped;
    }

    public int remove(Flags... substractedFlags) {
        return this.remove(null, substractedFlags);
    }
    public int remove(JSAGAFlags jsagaFlag, Flags... substractedFlags) {
        // set removed flags
        int removed = toInt(jsagaFlag, substractedFlags);

        // return substraction
        return m_flags - (m_flags & removed);
    }

    private static int toInt(JSAGAFlags jsagaFlag, Flags... flags) {
        int result = (jsagaFlag!=null ? jsagaFlag.getValue() : Flags.NONE.getValue());
        for (Flags f : flags) {
            result = f.or(result);
        }
        return result;
    }
}
