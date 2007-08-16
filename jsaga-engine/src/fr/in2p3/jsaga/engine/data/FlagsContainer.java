package fr.in2p3.jsaga.engine.data;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsContainer
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsContainer {
    private Flags m_flags;

    public FlagsContainer(Flags flags, Flags defaultFlags) {
        if (flags != null) {
            m_flags = flags;
        } else if (defaultFlags != null) {
            m_flags = defaultFlags;
        } else {
            m_flags = Flags.NONE;
        }
    }
    public FlagsContainer(Flags flags) {
        this(flags, null);
    }

    public void keepNamespaceEntryFlags() {
        m_flags = m_flags.and(Flags.all());
    }
    public void keepLogicalEntryFlags() {
        m_flags = m_flags.and(LogicalEntryFlags.all());
    }
    public void keepPhysicalEntryFlags() {
        m_flags = m_flags.and(PhysicalEntryFlags.all());
    }

    public boolean contains(Flags testedFlag) {
        return (m_flags.getValue() & testedFlag.getValue()) > 0 ||
                testedFlag.getValue() == Flags.NONE.getValue();
    }

    public void checkAllowed(Flags allowedFlags) throws BadParameter {
        int forbiddenFlags = m_flags.and(allowedFlags.not()).getValue();
        if (forbiddenFlags > 0) {
            throw new BadParameter("Flags not allowed for this method: "+forbiddenFlags);
        }
    }

    public void checkRequired(Flags requiredFlags) throws BadParameter {
        int missingFlags = requiredFlags.and(m_flags.not()).getValue();
        if (missingFlags > 0) {
            throw new BadParameter("Flags missing for this method: "+requiredFlags);
        }
    }

    public Flags remove(Flags removedFlags) {
        return m_flags.and(removedFlags.not());
    }
}
