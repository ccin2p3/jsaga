package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.URL;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.file.File;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryTest extends AbstractTest {
    // test data
    protected static final String DEFAULT_ROOTNAME = "root/";
    protected static final String DEFAULT_DIRNAME = "dir/";
    protected static final String DEFAULT_FILENAME = "file1.txt";
    protected static final String DEFAULT_FILEPATTERN = "file*";
    protected static final String DEFAULT_CONTENT = "Content of file 1...";
    protected static final String DEFAULT_PHYSICAL = "physical1.txt";
    protected static final String DEFAULT_PHYSICAL2 = "physical2.txt";
    protected static final int FLAGS_ROOT = Flags.CREATE.or(Flags.EXCL);
    protected static final int FLAGS_FILE = Flags.WRITE.or(Flags.EXCL.or(Flags.CREATEPARENTS));

    // configuration
    protected URL m_rootUrl;
    protected URL m_dirUrl;
    protected URL m_fileUrl;
    protected URL m_physicalRootUrl;
    protected URL m_physicalFileUrl;
    protected URL m_physicalFileUrl2;
    protected Session m_session;

    // setup
    protected NSDirectory m_root;
    protected NSEntry m_file;
    protected boolean m_toBeRemoved;

    public AbstractNSEntryTest(String protocol) throws Exception {
        super();

        // configure
        URL baseUrl = new URL(getRequiredProperty(protocol, CONFIG_BASE_URL).replaceAll(" ", "%20"));
        m_rootUrl = createURL(baseUrl, DEFAULT_ROOTNAME);
        m_dirUrl = createURL(m_rootUrl, DEFAULT_DIRNAME);
        m_fileUrl = createURL(m_dirUrl, DEFAULT_FILENAME);
        if (baseUrl.getFragment() != null) {
            m_session = SessionFactory.createSession(true);
        }
        if (getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL);
            URL basePhysicalUrl = new URL(getRequiredProperty(physicalProtocol, CONFIG_BASE_URL));
            m_physicalRootUrl = createURL(basePhysicalUrl, DEFAULT_ROOTNAME);
            m_physicalFileUrl = createURL(m_physicalRootUrl, DEFAULT_PHYSICAL);
            m_physicalFileUrl2 = createURL(m_physicalRootUrl, DEFAULT_PHYSICAL2);
        }
    }

    /** Implicitely invoked before executing each test method */
    protected void setUp() throws Exception {
        super.setUp();
        try {
            m_root = NSFactory.createNSDirectory(m_session, m_rootUrl, FLAGS_ROOT);
            m_file = m_root.open(m_fileUrl, FLAGS_FILE);
            if (m_file instanceof File) {
                Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
                ((File)m_file).write(buffer);
            } else if (m_file instanceof LogicalFile) {
                if (m_physicalFileUrl == null) {
                    throw new Exception("Configuration is missing required property: "+CONFIG_PHYSICAL_PROTOCOL);
                }
                ((LogicalFile)m_file).addLocation(m_physicalFileUrl);
            }
            m_file.close();
            m_toBeRemoved = true;
        } catch(NotImplemented e) {
            m_root = NSFactory.createNSDirectory(m_session, m_rootUrl, Flags.NONE.getValue());
            m_file = m_root.open(m_fileUrl, Flags.NONE.getValue());
            m_toBeRemoved = false;
        } catch(Exception e) {
//            try{this.tearDown();}catch(Exception e2){/**/}
            throw e;
        }
    }

    /** Implicitely invoked after executing each test method */
    protected void tearDown() throws Exception {
        if (m_file != null) {
            m_file.close();
        }
        if (m_toBeRemoved) {
            m_root.remove(Flags.RECURSIVE.getValue());
        }
        if (m_root != null) {
            m_root.close();
        }
        super.tearDown();
    }

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkWrited(URL url, String expected) throws Exception {
        Buffer buffer = BufferFactory.createBuffer(1024);
        File reader = (File) NSFactory.createNSEntry(m_session, url, Flags.READ.getValue());
        int len = reader.read(buffer);
        assertEquals(
                expected,
                new String(buffer.getData(), 0, len));
        reader.close(0);
    }
}
