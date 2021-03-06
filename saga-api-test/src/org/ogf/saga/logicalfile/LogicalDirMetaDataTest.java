package org.ogf.saga.logicalfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.abstracts.AbstractDirectory;
import org.ogf.saga.namespace.base.DirectoryBaseTest;
import org.ogf.saga.url.URL;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalDirectoryMetaDataTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Author: lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class LogicalDirMetaDataTest extends DirectoryBaseTest {
    private static final String METADATA_KEY = "myKey";
    private static final String METADATA_VALUE = "myValue";
    private static final String[] METADATA_FILTER = new String[]{METADATA_KEY+"="+METADATA_VALUE};
    private static final String[] METADATA_FILTER2 = new String[]{METADATA_KEY+"2"+"="+METADATA_VALUE+"2"};
    private LogicalDirectory m_dir;
    private LogicalDirectory m_subDir;
    private LogicalFile m_file;

    protected LogicalDirMetaDataTest(String protocol) throws Exception {
        super(protocol);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (super.m_dir instanceof LogicalDirectory) {
            m_dir = (LogicalDirectory) super.m_dir;
        }
        if (super.m_subDir instanceof LogicalDirectory) {
            m_subDir = (LogicalDirectory) super.m_subDir;
        }
        if (super.m_file instanceof LogicalFile) {
            m_file = (LogicalFile) super.m_file;
        }
        if (m_dir==null || m_subDir==null || m_file==null) {
            fail("Not a protocol for logical files");
        }
        m_subDir.setAttribute(METADATA_KEY, METADATA_VALUE);
        m_file.setAttribute(METADATA_KEY, METADATA_VALUE);
        m_file.setAttribute(METADATA_KEY+"2", METADATA_VALUE+"2");
    }

    @After
    public void tearDown() throws Exception {
        m_file = null;
        m_subDir = null;
        m_dir = null;
        super.tearDown();
    }

    @Test
    public void test_listAttributes() throws Exception {
        assertEquals(1, m_subDir.listAttributes().length);
        assertEquals(2, m_file.listAttributes().length);
    }

    @Test
    public void test_getAttribute() throws Exception {
        assertEquals(METADATA_VALUE, m_subDir.getAttribute(METADATA_KEY));
        assertEquals(METADATA_VALUE, m_file.getAttribute(METADATA_KEY));
        assertEquals(METADATA_VALUE+"2", m_file.getAttribute(METADATA_KEY+"2"));
    }

    @Test
    public void test_find() throws Exception {
        List<URL> list = m_subDir.find(null, METADATA_FILTER);
        assertEquals(1, list.size());
        assertEquals(DEFAULT_FILENAME, list.get(0).toString());
    }

    @Test
    public void test_find_norecurse() throws Exception {
        List<URL> list = m_dir.find(null, METADATA_FILTER, Flags.NONE.getValue());
        assertEquals(1, list.size());
        assertEquals(DEFAULT_SUBDIRNAME, list.get(0).toString());

        list = m_dir.find(null, METADATA_FILTER2, Flags.NONE.getValue());
        assertEquals(0, list.size());

        list = m_dir.find(DEFAULT_FILENAME, METADATA_FILTER, Flags.NONE.getValue());
        assertEquals(0, list.size());
    }

    @Test
    public void test_find_recurse() throws Exception {
        List<URL> list = m_dir.find(null, METADATA_FILTER2, Flags.RECURSIVE.getValue());
        assertEquals(1, list.size());
        assertEquals(DEFAULT_SUBDIRNAME+DEFAULT_FILENAME, list.get(0).toString());

        list = m_dir.find(null, METADATA_FILTER, Flags.RECURSIVE.getValue());
        assertEquals(2, list.size());

        list = m_dir.find(DEFAULT_FILENAME, METADATA_FILTER, Flags.RECURSIVE.getValue());
        assertEquals(1, list.size());

        String[] filter = new String[]{METADATA_KEY+"="+METADATA_VALUE, METADATA_KEY+"2"+"="+METADATA_VALUE+"2"};
        list = m_dir.find(null, filter, Flags.RECURSIVE.getValue());
        assertEquals(1, list.size());
        assertEquals(DEFAULT_SUBDIRNAME+DEFAULT_FILENAME, list.get(0).toString());
    }
}
