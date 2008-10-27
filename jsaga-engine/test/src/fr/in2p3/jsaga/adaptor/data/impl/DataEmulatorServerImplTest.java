package fr.in2p3.jsaga.adaptor.data.impl;

import fr.in2p3.jsaga.adaptor.schema.data.emulator.DirectoryType;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.File;
import junit.framework.TestCase;
import org.ogf.saga.error.DoesNotExistException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataEmulatorServerImplTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataEmulatorServerImplTest extends TestCase {
    private DataEmulatorConnection m_server;

    protected void setUp() throws Exception {
        // connect to server
        m_server = new DataEmulatorConnection("test", "emulator1.test.org", 1234);
        DirectoryType parent = m_server.getDirectory("/");
        parent = m_server.addDirectory(parent, "path");
        parent = m_server.addDirectory(parent, "to");
        File file = m_server.addFile(parent, "file1.txt");
        file.setContent("Content of file 1...");
    }

    protected void tearDown() throws Exception {
        // disconnect from server (no commit)
        m_server.removeFile("/path/to/file1.txt");
        m_server.removeDirectory("/path/to/");
        m_server.removeDirectory("/path");
        m_server = null;
    }

    public void test_getParentDirectory() throws DoesNotExistException {
        assertEquals(
                "to",
                m_server.getParentDirectory("/path/to/file1.txt").getName());
        assertEquals(
                "path",
                m_server.getParentDirectory("/path/to/").getName());
        assertEquals(
                "to",
                m_server.getParentDirectory("/path/to/unexisting file").getName());
    }

    public void test_getEntry() throws DoesNotExistException {
        assertEquals(
                "file1.txt",
                m_server.getEntry("/path/to/file1.txt").getName());
        assertEquals(
                "to",
                m_server.getEntry("/path/to/").getName());
        try {
            m_server.getEntry("/path/to/file2.txt");
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }

    public void test_listChildEntries() throws DoesNotExistException {
        assertEquals(
                "file1.txt",
                m_server.listEntries("/path/to/file1.txt")[0].getName());
        assertEquals(
                "file1.txt",
                m_server.listEntries("/path/to/")[0].getName());
        try {
            m_server.listEntries("/path/to/file2.txt");
            fail("Expected exception: "+ DoesNotExistException.class);
        } catch(DoesNotExistException e) {
        }
    }
}
