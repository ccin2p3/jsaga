package fr.in2p3.jsaga.impl.attributes;

import junit.framework.TestCase;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NotImplemented;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributeScalarTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AttributeScalarTest extends TestCase {
    public void testConstructor() throws NotImplemented, IncorrectState {
        assertEquals(
                "val1,val2",
                new AttributeScalar("key", new String[]{"val1", "val2"}).getValue()
        );
    }
}
