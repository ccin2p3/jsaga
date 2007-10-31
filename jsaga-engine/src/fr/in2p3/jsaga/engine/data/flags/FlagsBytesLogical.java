package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.namespace.Flags;

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

    private static FlagsBytesPhysical THESE_BYTES = READ.or(WRITE).or(READWRITE);

    public FlagsBytesLogical(int flags) {
        super(flags);
        this.value |= THESE_BYTES.getValue() & flags;
    }

    FlagsBytesLogical(Flags flags) {
        super(flags);
    }

    FlagsBytesLogical(int value, boolean internal) {
        super(value, internal);
    }
}
