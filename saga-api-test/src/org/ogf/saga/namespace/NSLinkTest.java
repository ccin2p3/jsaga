package org.ogf.saga.namespace;

import org.ogf.saga.URI;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryTest;

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
public class NSLinkTest extends AbstractNSEntryTest {
    // test data
    private static final String DEFAULT_LINKNAME = "link.txt";
    private static final String DEFAULT_LINKNAME_2 = "link2.txt";

    // configuration
    private URI m_linkUri;
    private URI m_linkUri2;

    // setup
    private NamespaceEntry m_link;

    public NSLinkTest(String protocol) throws Exception {
        super(protocol);
        m_linkUri = m_rootUri.resolve(DEFAULT_LINKNAME);
        m_linkUri2 = m_rootUri.resolve(DEFAULT_LINKNAME_2);
    }

    protected void setUp() throws Exception {
        super.setUp();
        m_file.link(m_linkUri, Flags.NONE);
        m_link = NamespaceFactory.createNamespaceEntry(m_session, m_linkUri, Flags.NONE);
    }

    /////////////////////////////////// file link tests ///////////////////////////////////

    public void test_isLink() throws Exception {
        assertFalse(m_file.isLink(Flags.NONE));
        assertTrue(m_link.isLink(Flags.NONE));
    }

    public void test_readLink() throws Exception {
        assertEquals(
                m_fileUri.toString(),
                m_link.readLink().toString());
    }

    public void test_link() throws Exception {
        m_link.link(m_linkUri2, Flags.NONE);
        NamespaceEntry link2 = NamespaceFactory.createNamespaceEntry(m_session, m_linkUri2, Flags.NONE);
        assertEquals(
                m_linkUri.toString(),
                link2.readLink().toString());
    }

    public void test_link_dereferenced() throws Exception {
        m_link.link(m_linkUri2, Flags.DEREFERENCE);
        NamespaceEntry link2 = NamespaceFactory.createNamespaceEntry(m_session, m_linkUri2, Flags.NONE);
        assertEquals(
                m_fileUri.toString(),
                link2.readLink().toString());
    }
}
