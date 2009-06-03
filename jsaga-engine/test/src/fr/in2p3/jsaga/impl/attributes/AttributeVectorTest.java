package fr.in2p3.jsaga.impl.attributes;

import junit.framework.TestCase;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NotImplementedException;

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
    public void testConstructor() throws NotImplementedException, IncorrectStateException {
        assertEquals(
                "val2",
                new AttributeVector("key", "val1"+Attribute.SEPARATOR+"val2").getValues()[1]
        );
    }
}
