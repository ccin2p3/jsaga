package org.ogf.saga.namespace;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.abstracts.AbstractData;
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
public abstract class EntryTest extends AbstractData {
    protected EntryTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_getURL() throws Exception {
        URL expected = m_fileUrl;
        Assert.assertEquals(
                expected.toString(),
                m_file.getURL().toString());
    }

    @Test
    public void test_getCWD() throws Exception {
        URL expected = URLFactory.createURL(m_subDirUrl.toString());
        expected.setFragment(null);
        Assert.assertEquals(
                expected.toString(),
                m_file.getCWD().toString());
    }

    @Test
    public void test_getName() throws Exception {
    	Assert.assertEquals(
                DEFAULT_FILENAME,
                m_file.getName().getPath());
    }

    @Test(expected = DoesNotExistException.class)
    public void test_unexisting() throws Exception {
        NSFactory.createNSEntry(m_session, createURL(m_subDirUrl, "unexisting.txt"), Flags.NONE.getValue());
    }
}
