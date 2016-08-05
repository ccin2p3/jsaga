package fr.in2p3.jsaga.impl.job.staging.mgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.JobDescription;

import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;

public class DataStagingManagerThroughStreamTest {

    private JobDescription m_base_desc;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void setUp() throws Exception {
        m_base_desc = new SAGAJobDescriptionImpl();
        m_base_desc.setAttribute(JobDescription.EXECUTABLE, "/opt/bin/myProgram");
        m_base_desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{});
        m_base_desc.setVectorAttribute(JobDescription.ARGUMENTS, new String[]{"arg1"});
        m_base_desc.setAttribute(JobDescription.INPUT, "file.in");
        m_base_desc.setAttribute(JobDescription.OUTPUT, "file.out");
        m_base_desc.setAttribute(JobDescription.ERROR, "file.err");
        m_base_desc.setAttribute(JobDescription.JOBPROJECT, "JSAGA-ut");
    }
    
    @Test
    public void modifyFileTransferRemoved() throws Exception {
        JobDescription newDesc = new DataStagingManagerThroughStream(m_base_desc.getVectorAttribute(JobDescription.FILETRANSFER))
                                    .modifyJobDescription(m_base_desc);
        assertFalse(newDesc.existsAttribute(JobDescription.FILETRANSFER));
    }
    
    @Test
    public void modifyRemote() throws Exception {
        m_base_desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myfile > remote://myfile"});
        JobDescription newDesc = new DataStagingManagerThroughStream(m_base_desc.getVectorAttribute(JobDescription.FILETRANSFER))
                                    .modifyJobDescription(m_base_desc);
        assertTrue(newDesc.existsAttribute(JobDescription.ARGUMENTS));
        assertTrue(newDesc.existsAttribute(JobDescription.INPUT));
        assertTrue(newDesc.existsAttribute(JobDescription.OUTPUT));
        assertTrue(newDesc.existsAttribute(JobDescription.ERROR));
        assertEquals("/opt/bin/myProgram", newDesc.getAttribute(JobDescription.EXECUTABLE));
        assertFalse(newDesc.existsAttribute(JobDescription.INTERACTIVE));
        assertEquals("JSAGA-ut", newDesc.getAttribute(JobDescription.JOBPROJECT));
    }
    
    // worker transfers and no executable => NoSuccessException
    @Test
    public void modifyNoExecutable() throws Exception {
        m_base_desc.removeAttribute(JobDescription.EXECUTABLE);
        m_base_desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myfile > /tmp/myfile"});
        exception.expect(NoSuccessException.class);
        new DataStagingManagerThroughStream(m_base_desc.getVectorAttribute(JobDescription.FILETRANSFER))
            .modifyJobDescription(m_base_desc);
    }
    
    @Test
    public void modifyInteractive() throws Exception {
        m_base_desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myfile > /tmp/myfile"});
        m_base_desc.setAttribute(JobDescription.INTERACTIVE, "true");
        exception.expect(BadParameterException.class);
        new DataStagingManagerThroughStream(m_base_desc.getVectorAttribute(JobDescription.FILETRANSFER))
            .modifyJobDescription(m_base_desc);
    }
    
    @Test
    public void modifyWorkerIn() throws Exception {
        this.modifyWorker("myfile > /tmp/myfile");
    }
    
    @Test
    public void modifyWorkerOut() throws Exception {
        this.modifyWorker("myfile < /tmp/myfile");
    }

    private void modifyWorker(String transfer) throws Exception {
        m_base_desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{transfer});
        JobDescription newDesc = new DataStagingManagerThroughStream(m_base_desc.getVectorAttribute(JobDescription.FILETRANSFER))
                                    .modifyJobDescription(m_base_desc);
        assertFalse(newDesc.existsAttribute(JobDescription.ARGUMENTS));
        assertFalse(newDesc.existsAttribute(JobDescription.INPUT));
        assertFalse(newDesc.existsAttribute(JobDescription.OUTPUT));
        assertFalse(newDesc.existsAttribute(JobDescription.ERROR));
        assertEquals("/bin/bash", newDesc.getAttribute(JobDescription.EXECUTABLE));
        assertEquals("true", newDesc.getAttribute(JobDescription.INTERACTIVE));
        assertEquals("JSAGA-ut", newDesc.getAttribute(JobDescription.JOBPROJECT));
    }

}
