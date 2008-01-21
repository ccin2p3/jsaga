package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.URL;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.SessionFactory;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSCopyTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSCopyTest extends AbstractNSDirectoryTest {
    protected static final int FLAGS_BYPASSEXIST = 4096;

    // test data
    protected static final String DEFAULT_ROOTNAME_2 = "root2/";
    protected static final String DEFAULT_FILENAME_2 = "file2.txt";
    protected static final String DEFAULT_CONTENT_2 = "Content of file 2 on base2.url...";

    // configuration
    protected URL m_rootUrl2;
    protected URL m_dirUrl2;
    protected URL m_fileUrl2;

    // setup
    protected NSDirectory m_root2;
    protected Directory m_physicalRoot;

    public AbstractNSCopyTest(String protocol, String targetProtocol) throws Exception {
        super(protocol);
        URL baseUrl2;
        if (protocol.equals(targetProtocol)) {
            if (getOptionalProperty(protocol, CONFIG_BASE2_URL) != null) {
                baseUrl2 = new URL(getOptionalProperty(protocol, CONFIG_BASE2_URL));
            } else {
                baseUrl2 = new URL(getRequiredProperty(protocol, CONFIG_BASE_URL));
            }
        } else {
            baseUrl2 = new URL(getRequiredProperty(targetProtocol, CONFIG_BASE_URL));
        }
        m_rootUrl2 = createURL(baseUrl2, DEFAULT_ROOTNAME_2);
        m_dirUrl2 = createURL(m_rootUrl2, DEFAULT_DIRNAME);
        m_fileUrl2 = createURL(m_dirUrl2, DEFAULT_FILENAME_2);
        if (m_session==null && baseUrl2.getFragment()!=null) {
            m_session = SessionFactory.createSession(true);
        }
        if (m_physicalRootUrl ==null && getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL);
            URL basePhysicalUrl = new URL(getRequiredProperty(physicalProtocol, CONFIG_BASE_URL));
            m_physicalRootUrl = createURL(basePhysicalUrl, DEFAULT_ROOTNAME);
            m_physicalFileUrl = createURL(m_physicalRootUrl, DEFAULT_PHYSICAL);
            m_physicalFileUrl2 = createURL(m_physicalRootUrl, DEFAULT_PHYSICAL2);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        try {
            if (m_rootUrl2 != null) {
                m_root2 = NSFactory.createNSDirectory(m_session, m_rootUrl2, FLAGS_ROOT);
                if (m_fileUrl2 != null) {
                    NSEntry file2 = m_root2.open(m_fileUrl2, FLAGS_FILE);
                    if (file2 instanceof File) {
                        Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                        ((File)file2).write(buffer);
                    } else if (file2 instanceof LogicalFile) {
                        ((LogicalFile)file2).addLocation(m_physicalFileUrl2);
                    }
                    file2.close();
                }
            }
            if (m_physicalRootUrl != null) {
                m_physicalRoot = (Directory) NSFactory.createNSDirectory(m_session, m_physicalRootUrl, FLAGS_ROOT);
                if (m_physicalFileUrl != null) {
                    File physicalFile = (File) m_physicalRoot.open(m_physicalFileUrl, FLAGS_FILE);
                    Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
                    physicalFile.write(buffer);
                    physicalFile.close(0);
                }
                if (m_physicalFileUrl2 != null) {
                    File physicalFile = (File) m_physicalRoot.open(m_physicalFileUrl2, FLAGS_FILE);
                    Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                    physicalFile.write(buffer);
                    physicalFile.close(0);
                }
            }
        } catch(Exception e) {
//            try{this.tearDown();}catch(Exception e2){/**/}
            throw e;
        }
    }

    protected void tearDown() throws Exception {
        if (m_root2 != null) {
            m_root2.remove(Flags.RECURSIVE.getValue());
            m_root2.close();
        }
        if (m_physicalRoot != null) {
            m_physicalRoot.remove(Flags.RECURSIVE.getValue());
            m_physicalRoot.close();
        }
        super.tearDown();
    }

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkCopied(URL url, String expectedContent) throws Exception {
        NSEntry entry = NSFactory.createNSEntry(m_session, url, Flags.READ.getValue());
        File reader;
        if (entry instanceof LogicalFile) {
            List<URL> physicalUrls = ((LogicalFile)entry).listLocations();
            assertNotNull(physicalUrls);
            assertTrue(physicalUrls.size() > 0);
            reader = (File) NSFactory.createNSEntry(m_session, physicalUrls.get(0), Flags.READ.getValue());
        } else {
            reader = (File) entry;
        }
        Buffer buffer = BufferFactory.createBuffer(1024);
        int len = reader.read(buffer);
        assertEquals(
                expectedContent,
                new String(buffer.getData(), 0, len));
        reader.close(0);
    }
}
