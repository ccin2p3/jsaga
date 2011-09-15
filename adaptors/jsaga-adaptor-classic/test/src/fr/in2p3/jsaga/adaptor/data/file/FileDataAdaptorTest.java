package fr.in2p3.jsaga.adaptor.data.file;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileDataAdaptorTest
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 aout 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "file",
                new FileDataAdaptor().getType());
    }
}
