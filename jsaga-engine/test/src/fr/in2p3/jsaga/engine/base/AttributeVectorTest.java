package fr.in2p3.jsaga.engine.base;

import junit.framework.TestCase;
import org.ogf.saga.error.NotImplemented;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AttributeVectorTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AttributeVectorTest extends TestCase {
    public void testConstructor() throws NotImplemented {
        assertEquals(
                "val2",
                new AttributeVector("key", "val1,val2").getValues()[1]
        );
    }
}
