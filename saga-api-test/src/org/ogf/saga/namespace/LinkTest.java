package org.ogf.saga.namespace;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;
import org.ogf.saga.namespace.base.DataBaseTest;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSLinkTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class LinkTest extends DataBaseTest {
    // test data
    private static final String DEFAULT_LINKNAME = "link.txt";
    private static final String DEFAULT_LINKNAME_2 = "link2.txt";

    // configuration
    private URL m_linkUrl;
    private URL m_linkUrl2;

    // setup
    private NSEntry m_link;

    protected LinkTest(String protocol) throws Exception {
        super(protocol);
        m_linkUrl = createURL(m_dirUrl, DEFAULT_LINKNAME);
        m_linkUrl2 = createURL(m_dirUrl, DEFAULT_LINKNAME_2);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        m_file.link(m_linkUrl, Flags.NONE.getValue());
        m_link = NSFactory.createNSEntry(m_session, m_linkUrl, Flags.NONE.getValue());
    }

    @After
    public void tearDown() throws Exception {
    	m_link.close();
    	super.tearDown();
    }
    /////////////////////////////////// file link tests ///////////////////////////////////

    @Test
    public void test_isLink() throws Exception {
    	Assert.assertFalse(m_file.isLink());
        Assert.assertTrue(m_link.isLink());
    }

    @Test
    public void test_readLink() throws Exception {
    	Assert.assertEquals(
                m_fileUrl.toString(),
                m_link.readLink().toString());
    }

    @Test
    public void test_link() throws Exception {
        m_link.link(m_linkUrl2, Flags.NONE.getValue());
        NSEntry link2 = NSFactory.createNSEntry(m_session, m_linkUrl2, Flags.NONE.getValue());
        Assert.assertEquals(
                m_linkUrl.toString(),
                link2.readLink().toString());
        link2.remove();
        link2.close();
    }

    @Test
    public void test_link_dereferenced() throws Exception {
        m_link.link(m_linkUrl2, Flags.DEREFERENCE.getValue());
        NSEntry link2 = NSFactory.createNSEntry(m_session, m_linkUrl2, Flags.NONE.getValue());
        Assert.assertEquals(
                m_fileUrl.toString(),
                link2.readLink().toString());
        link2.remove();
        link2.close();
    }
}
