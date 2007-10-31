package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.namespace.Flags;

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

    private static FlagsBytesPhysical THESE_BYTES = TRUNCATE.or(APPEND).or(BINARY);

    public FlagsBytesPhysical(int flags) {
        super(flags);
        this.value |= THESE_BYTES.getValue() & flags;
    }

    FlagsBytesPhysical(Flags flags) {
        super(flags);
    }

    FlagsBytesPhysical(int value, boolean internal) {
        super(value, internal);
    }
}
