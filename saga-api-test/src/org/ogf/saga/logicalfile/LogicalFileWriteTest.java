package org.ogf.saga.logicalfile;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceFactory;
import org.ogf.saga.namespace.abstracts.AbstractNSEntryWriteTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileWriteTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalFileWriteTest extends AbstractNSEntryWriteTest {
    public LogicalFileWriteTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_addLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
            // should be ignored (already exist)
            ((LogicalFile)m_file).addLocation(m_physicalFileUri);
            assertEquals(
                    1,
                    ((LogicalFile)m_file).listLocations().size());

            // add
            ((LogicalFile)m_file).addLocation(m_physicalFileUri2);
            assertEquals(
                    2,
                    ((LogicalFile)m_file).listLocations().size());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    public void test_removeLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
            // should throw an exception (does not exist)
            try {
                ((LogicalFile)m_file).removeLocation(m_physicalFileUri2);
                fail("Expected DoesNotExist exception");
            } catch(DoesNotExist e) {
                assertEquals(
                        1,
                        ((LogicalFile)m_file).listLocations().size());
            }

            // add
            ((LogicalFile)m_file).removeLocation(m_physicalFileUri);
            assertEquals(
                    0,
                    ((LogicalFile)m_file).listLocations().size());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    public void test_updateLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
            ((LogicalFile)m_file).updateLocation(m_physicalFileUri, m_physicalFileUri2);
            assertEquals(
                    1,
                    ((LogicalFile)m_file).listLocations().size());
            assertEquals(
                    m_physicalFileUri2.toString(),
                    ((LogicalFile)m_file).listLocations().get(0).toString());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    public void test_replicate() throws Exception {
        if (m_file instanceof LogicalFile) {
            // setUp()
            Directory physicalRoot = (Directory) NamespaceFactory.createNamespaceDirectory(m_session, m_physicalRootUri, FLAGS_ROOT);
            File physicalFile = (File) physicalRoot.open(m_physicalFileUri, FLAGS_FILE);
            Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT.getBytes());
            physicalFile.write(buffer.getSize(), buffer);
            physicalFile.close();

            // replicate
            ((LogicalFile)m_file).replicate(m_physicalFileUri2, Flags.NONE);
            assertEquals(
                    2,
                    ((LogicalFile)m_file).listLocations().size());

            // read new replica
            checkWrited(m_physicalFileUri2, DEFAULT_CONTENT);
            
            // tearDown()
            physicalRoot.remove(Flags.RECURSIVE);
            physicalRoot.close();
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }
}
