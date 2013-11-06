package org.ogf.saga.namespace.base;

import org.junit.After;
import org.junit.Before;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.file.File;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public abstract class DataMovementBaseTest extends DirBaseTest {
    protected static final int FLAGS_BYPASSEXIST = 4096;

    // test data
    protected static final String DEFAULT_DIRNAME_2 = "dir2/";
    protected static final String DEFAULT_FILENAME_2 = "file2.txt";
    protected static final String DEFAULT_CONTENT_2 = "Content of file 2 on base2.url...\n";

    // configuration
    protected URL m_dirUrl2;
    protected URL m_subDirUrl2;
    protected URL m_fileUrl2;

    // setup
    protected NSDirectory m_dir2;

    public DataMovementBaseTest(String protocol, String targetProtocol) throws Exception {
        super(protocol);
        URL baseUrl2;
        if (protocol.equals(targetProtocol)) {
            if (getOptionalProperty(protocol, CONFIG_BASE2_URL) != null) {
                baseUrl2 = URLFactory.createURL(getOptionalProperty(protocol, CONFIG_BASE2_URL));
            } else {
                baseUrl2 = URLFactory.createURL(getRequiredProperty(protocol, CONFIG_BASE_URL));
            }
        } else {
            baseUrl2 = URLFactory.createURL(getRequiredProperty(targetProtocol, CONFIG_BASE_URL));
        }
        m_dirUrl2 = createURL(baseUrl2, DEFAULT_DIRNAME_2);
        m_subDirUrl2 = createURL(m_dirUrl2, DEFAULT_SUBDIRNAME);
        m_fileUrl2 = createURL(m_subDirUrl2, DEFAULT_FILENAME_2);
        if (m_session==null && baseUrl2.getFragment()!=null) {
            m_session = SessionFactory.createSession(true);
        }
        if (m_physicalDirUrl ==null && getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL);
            URL basePhysicalUrl = URLFactory.createURL(getRequiredProperty(physicalProtocol, CONFIG_BASE_URL));
            m_physicalDirUrl = createURL(basePhysicalUrl, DEFAULT_DIRNAME);
            m_physicalFileUrl = createURL(m_physicalDirUrl, DEFAULT_PHYSICAL);
            m_physicalFileUrl2 = createURL(m_physicalDirUrl, DEFAULT_PHYSICAL2);
        }
    }

    @Before
	public void setUp() throws Exception {
        super.setUp();
        try {
        	if (m_physicalFileUrl2 != null) {
                // will be removed by super.tearDown()
                File physicalFile = (File) m_physicalDir.open(m_physicalFileUrl2, FLAGS_FILE);
                Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                physicalFile.write(buffer);
                physicalFile.close();
            }
            if (m_dirUrl2 != null) {
                // to be removed by this.tearDown()
                m_dir2 = NSFactory.createNSDirectory(m_session, m_dirUrl2, FLAGS_DIR);
                if (m_fileUrl2 != null) {
                    NSEntry file2 = m_dir2.open(m_fileUrl2, FLAGS_FILE);
                    if (file2 instanceof File) {
                        Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                        ((File)file2).write(buffer);
                    } else if (file2 instanceof LogicalFile) {
                        ((LogicalFile)file2).addLocation(m_physicalFileUrl2);
                    }
                    file2.close();
                }
            }
        } catch(Exception e) {
//            try{this.tearDown();}catch(Exception e2){/**/}
            throw e;
        }
    }

    @After
	public void tearDown() throws Exception {
        if (m_dir2 != null) {
            m_dir2.remove(Flags.RECURSIVE.getValue());
            m_dir2.close();
        }
        super.tearDown();
    }

    //////////////////////////////////////// protected methods ////////////////////////////////////////
    protected void checkCopied(URL url, String expectedContent) throws Exception {
    	checkCopied(url, expectedContent, 1024);
    }
    
    protected void checkCopied(URL url, String expectedContent, int bufferSize) throws Exception {
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
        Buffer buffer = BufferFactory.createBuffer(bufferSize);
        int len = reader.read(buffer);
        assertEquals(
                expectedContent.length(),
                len);
        assertEquals(
                expectedContent,
                new String(buffer.getData(), 0, len));
        reader.close();
        entry.close();
    }
}
