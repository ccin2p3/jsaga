package fr.in2p3.jsaga;

import org.ogf.saga.namespace.PhysicalEntryFlags;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ExtensionFlags
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ExtensionFlags extends PhysicalEntryFlags {
    public static final ExtensionFlags LATE_EXISTENCE_CHECK = new ExtensionFlags(4096);

    ExtensionFlags(int value) {
        super(value);
    }
}
