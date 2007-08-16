package fr.in2p3.jsaga.engine.data;

import junit.framework.TestCase;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsContainerTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsContainerTest extends TestCase {
    public void test_contructor() {
        assertFalse(new FlagsContainer(Flags.NONE, Flags.RECURSIVE).contains(Flags.RECURSIVE));
        assertTrue(new FlagsContainer(null, Flags.RECURSIVE).contains(Flags.RECURSIVE));
    }

    public void test_keepNamespaceEntryFlags() {
        FlagsContainer flags = new FlagsContainer(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL)), Flags.NONE);
        assertTrue(flags.contains(PhysicalEntryFlags.TRUNCATE));
        assertTrue(flags.contains(LogicalEntryFlags.READ));
        flags.keepNamespaceEntryFlags();
        assertFalse(flags.contains(PhysicalEntryFlags.TRUNCATE));
        assertFalse(flags.contains(LogicalEntryFlags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_keepLogicalEntryFlags() {
        FlagsContainer flags = new FlagsContainer(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL)), Flags.NONE);
        assertTrue(flags.contains(PhysicalEntryFlags.TRUNCATE));
        flags.keepLogicalEntryFlags();
        assertFalse(flags.contains(PhysicalEntryFlags.TRUNCATE));
        assertTrue(flags.contains(LogicalEntryFlags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_keepPhysicalEntryFlags() {
        FlagsContainer flags = new FlagsContainer(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL)), Flags.NONE);
        flags.keepPhysicalEntryFlags();
        assertTrue(flags.contains(PhysicalEntryFlags.TRUNCATE));
        assertTrue(flags.contains(LogicalEntryFlags.READ));
        assertTrue(flags.contains(Flags.EXCL));
    }

    public void test_contains() {
        assertFalse(new FlagsContainer(Flags.CREATE.or(Flags.EXCL), Flags.NONE).contains(Flags.RECURSIVE));
        assertTrue(new FlagsContainer(Flags.CREATE.or(Flags.EXCL), Flags.NONE).contains(Flags.EXCL));
    }

    public void test_checkAllowed() {
        FlagsContainer flags = new FlagsContainer(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL)), Flags.NONE);
        try {
            flags.checkAllowed(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL).or(Flags.RECURSIVE)));
        } catch(BadParameter e) {
            fail("Unexpected BadParameter exception: "+e.getMessage());
        }
        try {
            flags.checkAllowed(LogicalEntryFlags.READ.or(Flags.EXCL).or(Flags.RECURSIVE));
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
    }

    public void test_checkRequired() {
        FlagsContainer flags = new FlagsContainer(PhysicalEntryFlags.TRUNCATE.or(LogicalEntryFlags.READ.or(Flags.EXCL)), Flags.NONE);
        try {
            flags.checkRequired(LogicalEntryFlags.READ.or(Flags.EXCL));
        } catch(BadParameter e) {
            fail("Unexpected BadParameter exception: "+e.getMessage());
        }
        try {
            flags.checkRequired(LogicalEntryFlags.READ.or(Flags.EXCL).or(Flags.RECURSIVE));
            fail("Expected BadParameter exception");
        } catch(BadParameter e) {
        }
    }
}
