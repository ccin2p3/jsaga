package fr.in2p3.jsaga.impl.job.streaming.mgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.ogf.saga.job.JobDescription;

import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.impl.job.description.SAGAJobDescriptionImpl;
import fr.in2p3.jsaga.impl.job.streaming.LocalFileFactory;

public class StreamingManagerTest {

    private Mockery m_mockery = null;

    @Test
    public void modifyDescriptionOnePhase() throws Exception {
        m_mockery = new Mockery();
        final StagingJobAdaptorOnePhase sja = m_mockery.mock(StagingJobAdaptorOnePhase.class);
        m_mockery.checking(new Expectations() {{
            allowing(sja).getInputStagingTransfer(with(any(String.class))); will(returnValue(new StagingTransfer[]{})); 
            allowing(sja).getOutputStagingTransfer(with(any(String.class))); will(returnValue(new StagingTransfer[]{}));
            allowing(sja).getStagingDirectory(with(any(String.class))); will(returnValue(null));
            
        }});

        StreamingManagerThroughSandboxOnePhase manager = new StreamingManagerThroughSandboxOnePhase(sja, "123");
        assertEquals("worker-123.txt", manager.getWorker("txt"));
        // modify description and check
        JobDescription newJobDesc = manager.modifyJobDescription(new SAGAJobDescriptionImpl());
        assertEquals(JobDescription.FALSE, newJobDesc.getAttribute(JobDescription.INTERACTIVE));
        String[] transfers = newJobDesc.getVectorAttribute(JobDescription.FILETRANSFER);
        assertEquals(3, transfers.length);
        assertEquals(LocalFileFactory.getLocalInputFile("123").getAbsolutePath() + " > " + manager.getWorker("input"),
                transfers[0]);
        assertEquals(LocalFileFactory.getLocalOutputFile("123").getAbsolutePath() + " < " + manager.getWorker("output"),
                transfers[1]);
        assertEquals(LocalFileFactory.getLocalErrorFile("123").getAbsolutePath() + " < " + manager.getWorker("error"),
                transfers[2]);
        assertEquals(manager.getWorker("input"), newJobDesc.getAttribute(JobDescription.INPUT));
        assertEquals(manager.getWorker("output"), newJobDesc.getAttribute(JobDescription.OUTPUT));
        assertEquals(manager.getWorker("error"), newJobDesc.getAttribute(JobDescription.ERROR));
        // touch transfer files
        LocalFileFactory.getLocalInputFile("123").createNewFile();
        LocalFileFactory.getLocalOutputFile("123").createNewFile();
        LocalFileFactory.getLocalErrorFile("123").createNewFile();
        // cleanup
        manager.cleanup(null, "123");
        // check
        assertFalse(LocalFileFactory.getLocalInputFile("123").exists());
        assertFalse(LocalFileFactory.getLocalOutputFile("123").exists());
        assertFalse(LocalFileFactory.getLocalErrorFile("123").exists());
    }

    @Test
    public void modifyDescriptionTwoPhase() throws Exception {
        m_mockery = new Mockery();
        final StagingJobAdaptorTwoPhase sja = m_mockery.mock(StagingJobAdaptorTwoPhase.class);
        m_mockery.checking(new Expectations() {{
            allowing(sja).getInputStagingTransfer(with(any(String.class))); will(returnValue(new StagingTransfer[]{})); 
            allowing(sja).getOutputStagingTransfer(with(any(String.class))); will(returnValue(new StagingTransfer[]{}));
            allowing(sja).getStagingDirectory(with(any(String.class))); will(returnValue(null));
            
        }});
        StreamingManagerThroughSandboxTwoPhase manager = new StreamingManagerThroughSandboxTwoPhase(sja, "123");
        assertEquals("worker-123.txt", manager.getWorker("txt"));
        // modify description and check
        JobDescription newJobDesc = manager.modifyJobDescription(new SAGAJobDescriptionImpl());
        assertEquals(JobDescription.FALSE, newJobDesc.getAttribute(JobDescription.INTERACTIVE));
        String[] transfers = newJobDesc.getVectorAttribute(JobDescription.FILETRANSFER);
        assertEquals(3, transfers.length);
        assertEquals(LocalFileFactory.getLocalInputFile("123").getAbsolutePath() + " > " + manager.getWorker("input"),
                transfers[0]);
        assertEquals(LocalFileFactory.getLocalOutputFile("123").getAbsolutePath() + " < " + manager.getWorker("output"),
                transfers[1]);
        assertEquals(LocalFileFactory.getLocalErrorFile("123").getAbsolutePath() + " < " + manager.getWorker("error"),
                transfers[2]);
        assertEquals(manager.getWorker("input"), newJobDesc.getAttribute(JobDescription.INPUT));
        assertEquals(manager.getWorker("output"), newJobDesc.getAttribute(JobDescription.OUTPUT));
        assertEquals(manager.getWorker("error"), newJobDesc.getAttribute(JobDescription.ERROR));
        // touch transfer files
        LocalFileFactory.getLocalInputFile("123").createNewFile();
        LocalFileFactory.getLocalOutputFile("123").createNewFile();
        LocalFileFactory.getLocalErrorFile("123").createNewFile();
        // cleanup
        manager.cleanup(null, "123");
        // check
        assertFalse(LocalFileFactory.getLocalInputFile("123").exists());
        assertFalse(LocalFileFactory.getLocalOutputFile("123").exists());
        assertFalse(LocalFileFactory.getLocalErrorFile("123").exists());
    }
}
