package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.namespace.Flags;

import java.util.Collection;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsBytesPhysical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsBytesPhysical extends FlagsBytesLogical {
    public static final FlagsBytesPhysical TRUNCATE = new FlagsBytesPhysical(Flags.TRUNCATE);
    public static final FlagsBytesPhysical APPEND = new FlagsBytesPhysical(Flags.APPEND);
    public static final FlagsBytesPhysical BINARY = new FlagsBytesPhysical(Flags.BINARY);

    private static Flags[] THESE_FLAGS = new Flags[]{Flags.TRUNCATE, Flags.APPEND, Flags.BINARY};
    private static FlagsBytesPhysical THESE_BYTES = TRUNCATE.or(APPEND).or(BINARY);

    public FlagsBytesPhysical(Flags defaultFlag, Flags... flags) {
        super(defaultFlag, flags);
        for (Flags current : flags) {
            if (THESE_BYTES.contains(current)) {
                this.value |= current.getValue();
            }
        }
    }

    FlagsBytesPhysical(Flags flags) {
        super(flags);
    }

    FlagsBytesPhysical(int value) {
        super(value);
    }

    protected Collection<Flags> toFlags() {
        Collection<Flags> flags = super.toFlags();
        for (Flags current : THESE_FLAGS) {
            if (this.contains(current)) {
                flags.add(current);
            }
        }
        return flags;
    }
}
