package fr.in2p3.jsaga.engine.data.flags;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.namespace.Flags;

import java.util.ArrayList;
import java.util.Collection;

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

    private static final Flags[] THESE_FLAGS = new Flags[]{Flags.NONE, Flags.OVERWRITE, Flags.RECURSIVE, Flags.DEREFERENCE, Flags.CREATE, Flags.EXCL, Flags.LOCK, Flags.CREATEPARENTS};
    private static final FlagsBytesPhysical THESE_BYTES = NONE.or(OVERWRITE).or(RECURSIVE).or(DEREFERENCE).or(CREATE).or(EXCL).or(LOCK).or(CREATEPARENTS);

    protected int value;

    public FlagsBytes(Flags defaultFlag, Flags... flags) {
        // flags=(Flags)null  <=>  flags=new Flags[]{null}
        if (flags!=null && flags.length==1 && flags[0]==null) {
            flags = null;
        }

        // init
        if (flags != null) {
            this.value = NONE.getValue();
            for (Flags current : flags) {
                if (THESE_BYTES.contains(current)) {
                    this.value |= current.getValue();
                }
            }
        } else if (defaultFlag != null) {
            this.value = defaultFlag.getValue();
        } else {
            this.value = NONE.getValue();
        }
    }

    FlagsBytes(Flags flags) {
        this.value = flags.getValue();
    }

    FlagsBytes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean contains(Flags tested) {
        return (this.value & tested.getValue()) > 0 || tested.getValue()==Flags.NONE.getValue();
    }

    public void checkAllowed(Flags... allowed) throws BadParameter {
        FlagsBytes allowedFlags = new FlagsBytesPhysical(null, allowed);
        int forbiddenFlags = this.and(allowedFlags.not()).getValue();
        if (forbiddenFlags > 0) {
            throw new BadParameter("Flags not allowed for this method: "+forbiddenFlags);
        }
    }

    public void checkRequired(Flags... required) throws BadParameter {
        FlagsBytes requiredFlags = new FlagsBytesPhysical(null, required);
        int missingFlags = requiredFlags.and(this.not()).getValue();
        if (missingFlags > 0) {
            throw new BadParameter("Flags missing for this method: "+requiredFlags);
        }
    }

    public Flags[] remove(Flags... removed) throws BadParameter {
        FlagsBytes removedFlags = new FlagsBytesPhysical(null, removed);
        FlagsBytes remainingFlags = this.and(removedFlags.not());
        Collection<Flags> collection = remainingFlags.toFlags();
        return collection.toArray(new Flags[collection.size()]);
    }

    public Flags[] add(Flags... added) throws BadParameter {
        FlagsBytes addedFlags = new FlagsBytesPhysical(null, added);
        FlagsBytes totalFlags = this.or(addedFlags);
        Collection<Flags> collection = totalFlags.toFlags();
        return collection.toArray(new Flags[collection.size()]);
    }

    ////////////////////////////////// protected methods //////////////////////////////////

    public FlagsBytesPhysical or(FlagsBytes flags) {
        return new FlagsBytesPhysical(this.getValue() | flags.getValue());
    }

    protected FlagsBytesPhysical and(FlagsBytes flags) {
        return new FlagsBytesPhysical(this.getValue() & flags.getValue());
    }

    protected FlagsBytesPhysical not() {
        return new FlagsBytesPhysical(~ this.getValue());
    }

    protected Collection<Flags> toFlags() {
        Collection<Flags> flags = new ArrayList<Flags>();
        for (Flags current : THESE_FLAGS) {
            if (this.contains(current)) {
                flags.add(current);
            }
        }
        return flags;
    }
}
