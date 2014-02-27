package fr.in2p3.jsaga.impl.namespace;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.namespace.Flags;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   FlagsHelperTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class FlagsHelperTest {
    
    @Test
    public void test_checkAllowed() {
        int flags = Flags.TRUNCATE.or(Flags.READ.or(Flags.EXCL));
        try {
            new FlagsHelper(flags).allowed(Flags.TRUNCATE, Flags.READ, Flags.EXCL, Flags.RECURSIVE);
        } catch(BadParameterException e) {
            Assert.fail("Unexpected exception: "+ BadParameterException.class);
        }
        try {
            new FlagsHelper(flags).allowed(Flags.READ, Flags.EXCL, Flags.RECURSIVE);
            Assert.fail("Expected exception: "+ BadParameterException.class);
        } catch(BadParameterException e) {
        }
    }

    @Test
    public void test_checkRequired() {
        int flags = Flags.TRUNCATE.or(Flags.READ.or(Flags.EXCL));
        try {
            new FlagsHelper(flags).required(Flags.READ, Flags.EXCL);
        } catch(BadParameterException e) {
            Assert.fail("Unexpected exception: "+ BadParameterException.class);
        }
        try {
            new FlagsHelper(flags).required(Flags.READ, Flags.EXCL, Flags.RECURSIVE);
            Assert.fail("Expected exception: "+ BadParameterException.class);
        } catch(BadParameterException e) {
        }
    }

    @Test
    public void test_add() {
        int flags = Flags.CREATE.or(Flags.READ);
        int newFlags = new FlagsHelper(flags).add(Flags.READ, Flags.RECURSIVE);
        Assert.assertEquals(Flags.CREATE.or(Flags.READ.or(Flags.RECURSIVE)), newFlags);
    }

    @Test
    public void test_keep() {
        int flags = Flags.CREATE.or(Flags.APPEND);
        int newFlags = new FlagsHelper(flags).keep(Flags.ALLNAMESPACEFLAGS);
        Assert.assertEquals(Flags.CREATE.getValue(), newFlags);
    }

    @Test
    public void test_remove() {
        int flags = Flags.CREATE.or(Flags.APPEND);
        int newFlags = new FlagsHelper(flags).remove(Flags.ALLNAMESPACEFLAGS);
        Assert.assertEquals(Flags.APPEND.getValue(), newFlags);
    }
}
