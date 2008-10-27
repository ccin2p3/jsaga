package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.AbstractTest;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.File;
import org.ogf.saga.logicalfile.LogicalFile;
import org.ogf.saga.namespace.*;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
    protected static final String DEFAULT_DIRNAME = "dir/";
    protected static final String DEFAULT_SUBDIRNAME = "subdir/";
    protected static final String DEFAULT_FILENAME = "file1.txt";
    protected static final String DEFAULT_FILEPATTERN = "file*";
    protected static final String DEFAULT_CONTENT = "Content of file 1...\n";
    protected static final String DEFAULT_PHYSICAL = "physical1.txt";
    protected static final String DEFAULT_PHYSICAL2 = "physical2.txt";
    protected static final int FLAGS_DIR = Flags.CREATE.or(Flags.EXCL);
    protected static final int FLAGS_FILE = Flags.WRITE.or(Flags.EXCL.or(Flags.CREATEPARENTS));

    // configuration
    protected URL m_dirUrl;
    protected URL m_subDirUrl;
    protected URL m_fileUrl;
    protected URL m_physicalDirUrl;
    protected URL m_physicalFileUrl;
    protected URL m_physicalFileUrl2;
    protected Session m_session;

    // setup
    protected NSDirectory m_dir;
    protected NSEntry m_file;
    protected boolean m_toBeRemoved;

    public AbstractNSEntryTest(String protocol) throws Exception {
        super();

        // configure
        URL baseUrl = URLFactory.createURL(getRequiredProperty(protocol, CONFIG_BASE_URL).replaceAll(" ", "%20"));
        m_dirUrl = createURL(baseUrl, DEFAULT_DIRNAME);
        m_subDirUrl = createURL(m_dirUrl, DEFAULT_SUBDIRNAME);
        m_fileUrl = createURL(m_subDirUrl, DEFAULT_FILENAME);
        m_session = SessionFactory.createSession(true);
        if (getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL);
            URL basePhysicalUrl = URLFactory.createURL(getRequiredProperty(physicalProtocol, CONFIG_BASE_URL));
            m_physicalDirUrl = createURL(basePhysicalUrl, DEFAULT_DIRNAME);
            m_physicalFileUrl = createURL(m_physicalDirUrl, DEFAULT_PHYSICAL);
            m_physicalFileUrl2 = createURL(m_physicalDirUrl, DEFAULT_PHYSICAL2);
        }
    }

    /** Implicitely invoked before executing each test method */
    protected void setUp() throws Exception {
        super.setUp();
        try {
            // read-write protocol
            m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, FLAGS_DIR);
            m_file = m_dir.open(m_fileUrl, FLAGS_FILE);
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
        } catch(NotImplementedException e) {
            // read-only protocol
            try {
                m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.NONE.getValue());
                m_file = m_dir.open(m_fileUrl, Flags.NONE.getValue());
                m_toBeRemoved = false;
            } catch(DoesNotExistException e2) {
                throw new DoesNotExistException("Please create the expected files and directories for test suite (http://grid.in2p3.fr/jsaga-dev/faq.html#create-test-entries)", e2);
            }
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
            m_dir.remove(Flags.RECURSIVE.getValue());
        }
        if (m_dir != null) {
            m_dir.close();
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
