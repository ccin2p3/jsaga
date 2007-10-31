package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.namespace.Flags;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsBytes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsBytes {
    public static final FlagsBytesPhysical NONE = new FlagsBytesPhysical(Flags.NONE);
    public static final FlagsBytesPhysical OVERWRITE = new FlagsBytesPhysical(Flags.OVERWRITE);
    public static final FlagsBytesPhysical RECURSIVE = new FlagsBytesPhysical(Flags.RECURSIVE);
    public static final FlagsBytesPhysical DEREFERENCE = new FlagsBytesPhysical(Flags.DEREFERENCE);
    public static final FlagsBytesPhysical CREATE = new FlagsBytesPhysical(Flags.CREATE);
    public static final FlagsBytesPhysical EXCL = new FlagsBytesPhysical(Flags.EXCL);
    public static final FlagsBytesPhysical LOCK = new FlagsBytesPhysical(Flags.LOCK);
    public static final FlagsBytesPhysical CREATEPARENTS = new FlagsBytesPhysical(Flags.CREATEPARENTS);

    private static final FlagsBytesPhysical THESE_BYTES = NONE.or(OVERWRITE).or(RECURSIVE).or(DEREFERENCE).or(CREATE).or(EXCL).or(LOCK).or(CREATEPARENTS);

    protected int value;

    public FlagsBytes(int flags) {
        this.value = THESE_BYTES.getValue() & flags;
    }

    FlagsBytes(Flags flags) {
        this.value = flags.getValue();
    }

    FlagsBytes(int value, boolean internal) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean contains(Flags tested) {
        return (this.value & tested.getValue()) > 0 || tested.getValue()==Flags.NONE.getValue();
    }

    public void checkAllowed(int allowed) throws BadParameter {
        FlagsBytes allowedFlags = new FlagsBytesPhysical(allowed);
        int forbiddenFlags = this.and(allowedFlags.not()).getValue();
        if (forbiddenFlags > 0) {
            throw new BadParameter("Flags not allowed for this method: "+forbiddenFlags);
        }
    }

    public void checkRequired(int required) throws BadParameter {
        FlagsBytes requiredFlags = new FlagsBytesPhysical(required);
        int missingFlags = requiredFlags.and(this.not()).getValue();
        if (missingFlags > 0) {
            throw new BadParameter("Flags missing for this method: "+missingFlags);
        }
    }

    public int remove(Flags removed) throws BadParameter {
        FlagsBytes removedFlags = new FlagsBytesPhysical(removed);
        FlagsBytes remainingFlags = this.and(removedFlags.not());
        return remainingFlags.getValue();
    }

    public int add(Flags added) throws BadParameter {
        FlagsBytes addedFlags = new FlagsBytesPhysical(added);
        FlagsBytes totalFlags = this.or(addedFlags);
        return totalFlags.getValue();
    }

    ////////////////////////////////// protected methods //////////////////////////////////

    public FlagsBytesPhysical or(FlagsBytes flags) {
        return new FlagsBytesPhysical(this.getValue() | flags.getValue(), true);
    }

    private FlagsBytesPhysical and(FlagsBytes flags) {
        return new FlagsBytesPhysical(this.getValue() & flags.getValue(), true);
    }

    private FlagsBytesPhysical not() {
        return new FlagsBytesPhysical(~ this.getValue(), true);
    }
}
