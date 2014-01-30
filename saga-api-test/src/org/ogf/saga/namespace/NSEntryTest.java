package org.ogf.saga.namespace;

import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
@Deprecated
public abstract class NSEntryTest extends AbstractNSEntryTest {
    protected NSEntryTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_getURL() throws Exception {
        URL expected = m_fileUrl;
        assertEquals(
                expected.toString(),
                m_file.getURL().toString());
    }

    public void test_getCWD() throws Exception {
        URL expected = URLFactory.createURL(m_subDirUrl.toString());
        expected.setFragment(null);
        assertEquals(
                expected.toString(),
                m_file.getCWD().toString());
    }

    public void test_getName() throws Exception {
        assertEquals(
                DEFAULT_FILENAME,
                m_file.getName().getPath());
    }

    public void test_unexisting() throws Exception {
        try {
            NSFactory.createNSEntry(m_session, createURL(m_subDirUrl, "unexisting.txt"), Flags.NONE.getValue());
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }
}
