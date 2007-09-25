package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.namespace.Flags;

import java.util.Collection;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsBytesLogical
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsBytesLogical extends FlagsBytes {
    public static final FlagsBytesPhysical READ = new FlagsBytesPhysical(Flags.READ);
    public static final FlagsBytesPhysical WRITE = new FlagsBytesPhysical(Flags.WRITE);
    public static final FlagsBytesPhysical READWRITE = new FlagsBytesPhysical(Flags.READWRITE);  // READWRITE = READ | WRITE

    private static Flags[] THESE_FLAGS = new Flags[]{Flags.READ, Flags.WRITE, Flags.READWRITE};
    private static FlagsBytesPhysical THESE_BYTES = READ.or(WRITE).or(READWRITE);

    public FlagsBytesLogical(Flags defaultFlag, Flags... flags) {
        super(defaultFlag, flags);
        for (Flags current : flags) {
            if (THESE_BYTES.contains(current)) {
                this.value |= current.getValue();
            }
        }
    }

    FlagsBytesLogical(Flags flags) {
        super(flags);
    }

    FlagsBytesLogical(int value) {
        super(value);
    }

    protected Collection<Flags> toFlags() {
        Collection<Flags> flags = super.toFlags();
        for (Flags current : THESE_FLAGS) {
            if (this.contains(current) && current!=Flags.READWRITE) {
                flags.add(current);
            }
        }
        return flags;
    }
}
