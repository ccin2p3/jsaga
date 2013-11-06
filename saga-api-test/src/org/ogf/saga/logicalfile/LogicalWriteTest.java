package org.ogf.saga.logicalfile;

import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.base.WriteBaseTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalFileWriteTest
* Author: Lionel.schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class LogicalWriteTest extends WriteBaseTest {
    protected LogicalWriteTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_addLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
            // should be ignored (already exist)
            ((LogicalFile)m_file).addLocation(m_physicalFileUrl);
            assertEquals(
                    1,
                    ((LogicalFile)m_file).listLocations().size());

            
            //Create m_physicalFileUrl2... will be removed by tearDown()
            if(m_physicalFileUrl2 != null && !new java.io.File(m_physicalFileUrl2.getString()).exists()){
	            File physicalFile = (File) m_physicalDir.open(m_physicalFileUrl2, FLAGS_FILE);
	            Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
	            physicalFile.write(buffer);
	            physicalFile.close();
            }

            // add
            ((LogicalFile)m_file).addLocation(m_physicalFileUrl2);
            assertEquals(
                    2,
                    ((LogicalFile)m_file).listLocations().size());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    @Test
    public void test_removeLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
            // should throw an exception (does not exist)
            try {
                ((LogicalFile)m_file).removeLocation(m_physicalFileUrl2);
                fail("Expected exception: "+ DoesNotExistException.class);
            } catch(DoesNotExistException e) {
                assertEquals(
                        1,
                        ((LogicalFile)m_file).listLocations().size());
            }

            // remove
            ((LogicalFile)m_file).removeLocation(m_physicalFileUrl);
            assertEquals(
                    0,
                    ((LogicalFile)m_file).listLocations().size());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    @Test
    public void test_updateLocation() throws Exception {
        if (m_file instanceof LogicalFile) {
        	//Create m_physicalFileUrl2... will be removed by tearDown()
            if(m_physicalFileUrl2 != null && !new java.io.File(m_physicalFileUrl2.getString()).exists()){
	            File physicalFile = (File) m_physicalDir.open(m_physicalFileUrl2, FLAGS_FILE);
	            Buffer buffer = BufferFactory.createBuffer(DEFAULT_CONTENT2.getBytes());
	            physicalFile.write(buffer);
	            physicalFile.close();
            }
            
            ((LogicalFile)m_file).updateLocation(m_physicalFileUrl, m_physicalFileUrl2);
            assertEquals(
                    1,
                    ((LogicalFile)m_file).listLocations().size());
            assertEquals(
                    m_physicalFileUrl2.toString(),
                    ((LogicalFile)m_file).listLocations().get(0).toString());
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }

    @Test
    public void test_replicate() throws Exception {
        if (m_file instanceof LogicalFile) {
            // replicate
            ((LogicalFile)m_file).replicate(m_physicalFileUrl2, Flags.NONE.getValue());
            assertEquals(
                    2,
                    ((LogicalFile)m_file).listLocations().size());

            // read new replica
            checkWrited(m_physicalFileUrl2, DEFAULT_CONTENT);
        } else {
            fail("Not an instance of class: LogicalFile");
        }
    }
}
