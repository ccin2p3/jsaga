package org.ogf.saga.namespace;

import org.ogf.saga.URL;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSEntryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSEntryTest extends AbstractNSEntryTest {
    public NSEntryTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_getURL() throws Exception {
        URL expected = new URL(m_fileUrl.toString());
        expected.setFragment(null);
        assertEquals(
                expected.toString(),
                m_file.getURL().toString());
    }

    public void test_getCWD() throws Exception {
        assertEquals(
                createURL(m_rootUrl, DEFAULT_DIRNAME).getPath(),
                m_file.getCWD());
    }

    public void test_getName() throws Exception {
        assertEquals(
                DEFAULT_FILENAME,
                m_file.getName());
    }
}
