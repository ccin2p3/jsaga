package org.ogf.saga.logicalfile;

import org.ogf.saga.namespace.base.ReadBaseTest;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileReadTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class LogicalReadTest extends ReadBaseTest {
    protected LogicalReadTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_listLocations() throws Exception {
        if (m_file instanceof LogicalFile) {
            List<URL> locations = ((LogicalFile)m_file).listLocations();
            assertEquals(
                    1,
                    locations.size());
            assertEquals(
                    m_physicalFileUrl.toString(),
                    locations.get(0).toString());
        } else {
        	fail("Not an instance of class: LogicalFile");
        }
    }
}
