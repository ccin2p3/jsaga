package fr.in2p3.jsaga.adaptor.data;

import junit.framework.TestCase;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   LinuxDataAdaptorTest
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class LinuxDataAdaptorTest extends TestCase {
    public void test_getScheme() {
        assertEquals(
                "linux",
                new LinuxDataAdaptor().getType());
    }
    public void test_getGroups() throws Exception {
    	LinuxDataAdaptor lda = new LinuxDataAdaptor();
    	lda.connect(null, null, 0, null, null);
    	assertTrue(lda.getGroupsOf(System.getProperty("user.name")).length > 0);
    	lda.disconnect();
    }
}
