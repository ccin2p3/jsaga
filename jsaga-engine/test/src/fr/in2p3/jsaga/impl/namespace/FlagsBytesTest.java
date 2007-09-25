package fr.in2p3.jsaga.impl.namespace;

import junit.framework.TestCase;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.namespace.Flags;
import fr.in2p3.jsaga.engine.data.flags.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsBytesTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsBytesTest extends TestCase {
    public void test_contructor() {
        assertFalse(new FlagsBytes(Flags.RECURSIVE, Flags.NONE).contains(Flags.RECURSIVE));
        assertTrue(new FlagsBytes(Flags.RECURSIVE, (Flags)null).contains(Flags.RECURSIVE));
    }

    public void test_filterNonBaseFlags() {
        FlagsBytes flags = new FlagsBytes(null, Flags.TRUNCATE, Flags.READ, Flags.EXCL);
        assertFalse(flags.contains(Flags.TRUNCATE));
        assertFalse(flags.contains(Flags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_filterNonLogicalFlags() {
        FlagsBytes flags = new FlagsBytesLogical(null, Flags.TRUNCATE, Flags.READ, Flags.EXCL);
        assertFalse(flags.contains(Flags.TRUNCATE));
        assertTrue(flags.contains(Flags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_filterNonPhysicalFlags() {
        FlagsBytes flags = new FlagsBytesPhysical(null, Flags.TRUNCATE, Flags.READ, Flags.EXCL);
        assertTrue(flags.contains(Flags.TRUNCATE));
        assertTrue(flags.contains(Flags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_contains() {
        FlagsBytes flags = new FlagsBytes(null, Flags.CREATE, Flags.EXCL);
        assertFalse(flags.contains(Flags.RECURSIVE));
        flags = new FlagsBytes(null, Flags.CREATE, Flags.EXCL);
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_checkAllowed() {
        FlagsBytes flags = new FlagsBytesPhysical(null, Flags.TRUNCATE, Flags.READ, Flags.EXCL);
        try {
            flags.checkAllowed(Flags.TRUNCATE, Flags.READ, Flags.EXCL ,Flags.RECURSIVE);
        } catch(BadParameter e) {
            fail("Unexpected BadParameter exception: "+e.getMessage());
        }
        try {
            flags.checkAllowed(Flags.READ, Flags.EXCL, Flags.RECURSIVE);
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
    }

    public void test_checkRequired() {
        FlagsBytes flags = new FlagsBytesPhysical(null, Flags.TRUNCATE, Flags.READ, Flags.EXCL);
        try {
            flags.checkRequired(Flags.READ, Flags.EXCL);
        } catch(BadParameter e) {
            fail("Unexpected BadParameter exception: "+e.getMessage());
        }
        try {
            flags.checkRequired(Flags.READ, Flags.EXCL, Flags.RECURSIVE);
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
    }
}
