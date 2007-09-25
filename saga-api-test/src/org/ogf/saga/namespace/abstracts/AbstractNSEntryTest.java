package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.URI;
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
public abstract class AbstractNSEntryTest extends AbstractNSTest {
    // test data
    protected static final String DEFAULT_ROOTNAME = "root/";
    protected static final String DEFAULT_DIRNAME = "dir/";
    protected static final String DEFAULT_FILENAME = "file1.txt";
    protected static final String DEFAULT_FILEPATTERN = "file*";
    protected static final String DEFAULT_CONTENT = "Content of file 1...";
    protected static final String DEFAULT_PHYSICAL = "physical1.txt";
    protected static final String DEFAULT_PHYSICAL2 = "physical2.txt";
    protected static final Flags[] FLAGS_ROOT = new Flags[]{Flags.CREATE, Flags.EXCL};
    protected static final Flags[] FLAGS_FILE = new Flags[]{Flags.WRITE, Flags.EXCL, Flags.CREATEPARENTS};

    // configuration
    protected URI m_rootUri;
    protected URI m_dirUri;
    protected URI m_fileUri;
    protected URI m_physicalRootUri;
    protected URI m_physicalFileUri;
    protected URI m_physicalFileUri2;
    protected Session m_session;

    // setup
    protected NamespaceDirectory m_root;
    protected NamespaceEntry m_file;
    protected boolean m_toBeRemoved;

    public AbstractNSEntryTest(String protocol) throws Exception {
        super();

        // configure
        URI baseUri = new URI(getRequiredProperty(protocol, CONFIG_BASE_URI));
        m_rootUri = baseUri.resolve(DEFAULT_ROOTNAME);
        m_dirUri = m_rootUri.resolve(DEFAULT_DIRNAME);
        m_fileUri = m_dirUri.resolve(DEFAULT_FILENAME);
        if (baseUri.getFragment() != null) {
            m_session = SessionFactory.createSession(true);
        }
        if (getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(protocol, CONFIG_PHYSICAL_PROTOCOL);
            URI basePhysicalUri = new URI(getRequiredProperty(physicalProtocol, CONFIG_BASE_URI));
            m_physicalRootUri = basePhysicalUri.resolve(DEFAULT_ROOTNAME);
            m_physicalFileUri = m_physicalRootUri.resolve(DEFAULT_PHYSICAL);
            m_physicalFileUri2 = m_physicalRootUri.resolve(DEFAULT_PHYSICAL2);
        }
    }

    /** Implicitely invoked before executing each test method */
    protected void setUp() throws Exception {
        super.setUp();
        try {
            m_root = NamespaceFactory.createNamespaceDirectory(m_session, m_rootUri, FLAGS_ROOT);
            m_file = m_root.open(m_fileUri, FLAGS_FILE);
            if (m_file instanceof File) {
                Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
                ((File)m_file).write(buffer.getSize(), buffer);
            } else if (m_file instanceof LogicalFile) {
                if (m_physicalFileUri == null) {
                    throw new Exception("Configuration is missing required property: "+CONFIG_PHYSICAL_PROTOCOL);
                }
                ((LogicalFile)m_file).addLocation(m_physicalFileUri);
            }
            m_file.close();
            m_toBeRemoved = true;
        } catch(NotImplemented e) {
            m_root = NamespaceFactory.createNamespaceDirectory(m_session, m_rootUri, Flags.NONE);
            m_file = m_root.open(m_fileUri, Flags.NONE);
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
            m_root.remove(Flags.RECURSIVE);
        }
        if (m_root != null) {
            m_root.close();
        }
        super.tearDown();
    }

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkWrited(URI uri, String expected) throws Exception {
        Buffer buffer = BufferFactory.createBuffer(1024);
        File reader = (File) NamespaceFactory.createNamespaceEntry(m_session, uri, Flags.READ);
        int len = reader.read(1024, buffer);
        assertEquals(
                expected,
                new String(buffer.getData(), 0, len));
        reader.close(0);
    }
}
