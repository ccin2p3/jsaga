package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.URI;
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
    // test data
    protected static final String DEFAULT_ROOTNAME_2 = "root2/";
    protected static final String DEFAULT_FILENAME_2 = "file2.txt";
    protected static final String DEFAULT_CONTENT_2 = "Content of file 2 on base2.uri...";

    // configuration
    protected URI m_rootUri2;
    protected URI m_dirUri2;
    protected URI m_fileUri2;

    // setup
    protected NamespaceDirectory m_root2;
    protected Directory m_physicalRoot;

    public AbstractNSCopyTest(String protocol, String targetProtocol) throws Exception {
        super(protocol);
        URI baseUri2;
        if (protocol.equals(targetProtocol)) {
            if (getOptionalProperty(protocol, CONFIG_BASE2_URI) != null) {
                baseUri2 = new URI(getOptionalProperty(protocol, CONFIG_BASE2_URI));
            } else {
                baseUri2 = new URI(getRequiredProperty(protocol, CONFIG_BASE_URI));
            }
        } else {
            baseUri2 = new URI(getRequiredProperty(targetProtocol, CONFIG_BASE_URI));
        }
        m_rootUri2 = baseUri2.resolve(DEFAULT_ROOTNAME_2);
        m_dirUri2 = m_rootUri2.resolve(DEFAULT_DIRNAME);
        m_fileUri2 = m_dirUri2.resolve(DEFAULT_FILENAME_2);
        if (m_session==null && baseUri2.getFragment()!=null) {
            m_session = SessionFactory.createSession(true);
        }
        if (m_physicalRootUri==null && getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL) != null) {
            String physicalProtocol = getOptionalProperty(targetProtocol, CONFIG_PHYSICAL_PROTOCOL);
            URI basePhysicalUri = new URI(getRequiredProperty(physicalProtocol, CONFIG_BASE_URI));
            m_physicalRootUri = basePhysicalUri.resolve(DEFAULT_ROOTNAME);
            m_physicalFileUri = m_physicalRootUri.resolve(DEFAULT_PHYSICAL);
            m_physicalFileUri2 = m_physicalRootUri.resolve(DEFAULT_PHYSICAL2);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        try {
            if (m_rootUri2 != null) {
                m_root2 = NamespaceFactory.createNamespaceDirectory(m_session, m_rootUri2, FLAGS_ROOT);
                if (m_fileUri2 != null) {
                    NamespaceEntry file2 = m_root2.open(m_fileUri2, FLAGS_FILE);
                    if (file2 instanceof File) {
                        Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                        ((File)file2).write(buffer.getSize(), buffer);
                    } else if (file2 instanceof LogicalFile) {
                        ((LogicalFile)file2).addLocation(m_physicalFileUri2);
                    }
                    file2.close();
                }
            }
            if (m_physicalRootUri != null) {
                m_physicalRoot = (Directory) NamespaceFactory.createNamespaceDirectory(m_session, m_physicalRootUri, FLAGS_ROOT);
                if (m_physicalFileUri != null) {
                    File physicalFile = (File) m_physicalRoot.open(m_physicalFileUri, FLAGS_FILE);
                    Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
                    physicalFile.write(buffer.getSize(), buffer);
                    physicalFile.close(0);
                }
                if (m_physicalFileUri2 != null) {
                    File physicalFile = (File) m_physicalRoot.open(m_physicalFileUri2, FLAGS_FILE);
                    Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT_2.getBytes());
                    physicalFile.write(buffer.getSize(), buffer);
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
            m_root2.remove(Flags.RECURSIVE);
            m_root2.close();
        }
        if (m_physicalRoot != null) {
            m_physicalRoot.remove(Flags.RECURSIVE);
            m_physicalRoot.close();
        }
        super.tearDown();
    }

    //////////////////////////////////////// protected methods ////////////////////////////////////////

    protected void checkCopied(URI uri, String expectedContent) throws Exception {
        NamespaceEntry entry = NamespaceFactory.createNamespaceEntry(m_session, uri, Flags.READ);
        File reader;
        if (entry instanceof LogicalFile) {
            List<URI> physicalUris = ((LogicalFile)entry).listLocations();
            assertNotNull(physicalUris);
            assertTrue(physicalUris.size() > 0);
            reader = (File) NamespaceFactory.createNamespaceEntry(m_session, physicalUris.get(0), Flags.READ);
        } else {
            reader = (File) entry;
        }
        Buffer buffer = BufferFactory.createBuffer(1024);
        int len = reader.read(1024, buffer);
        assertEquals(
                expectedContent,
                new String(buffer.getData(), 0, len));
        reader.close(0);
    }
}
