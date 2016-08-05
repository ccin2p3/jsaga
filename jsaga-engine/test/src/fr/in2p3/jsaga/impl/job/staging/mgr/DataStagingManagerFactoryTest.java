package fr.in2p3.jsaga.impl.job.staging.mgr;

import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.job.JobDescription;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorOnePhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;

public class DataStagingManagerFactoryTest {

    private static Mockery m_mockery;
    private static JobDescription m_desc_staging;
    private static JobDescription m_desc_interactive;
    private static JobDescription m_desc_simple;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @BeforeClass
    public static void init() throws Exception {
        m_mockery = new Mockery() {{
            // multithread support for run()
            setThreadingPolicy(new Synchroniser());
        }};
        m_desc_staging = m_mockery.mock(JobDescription.class, "desc-staging");
        m_desc_interactive = m_mockery.mock(JobDescription.class, "desc-interactive");
        m_desc_simple = m_mockery.mock(JobDescription.class, "desc-simple");
        m_mockery.checking(new Expectations() {{
            allowing(m_desc_staging).getVectorAttribute(JobDescription.FILETRANSFER); will(returnValue(new String[]{}));
            allowing(m_desc_interactive).getVectorAttribute(JobDescription.FILETRANSFER); will(throwException(new DoesNotExistException()));
            allowing(m_desc_interactive).getAttribute(JobDescription.INTERACTIVE); will(returnValue("true"));
            allowing(m_desc_simple).getVectorAttribute(JobDescription.FILETRANSFER); will(throwException(new DoesNotExistException()));
            allowing(m_desc_simple).getAttribute(JobDescription.INTERACTIVE); will(returnValue("false"));
        }});
    }

    @Test
    public void onePhase() throws Exception {
        final JobControlAdaptor adaptor = m_mockery.mock(StagingJobAdaptorOnePhase.class, "one-phase");
        assertTrue(DataStagingManagerFactory.create(adaptor) instanceof DataStagingManagerThroughSandboxOnePhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_staging, "uniqId") instanceof DataStagingManagerThroughSandboxOnePhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_interactive, "uniqId") instanceof DataStagingManagerThroughSandboxOnePhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_simple, "uniqId") instanceof DataStagingManagerDummy);
    }

    @Test
    public void twoPhase() throws Exception {
        final JobControlAdaptor adaptor = m_mockery.mock(StagingJobAdaptorTwoPhase.class, "two-phase");
        assertTrue(DataStagingManagerFactory.create(adaptor) instanceof DataStagingManagerThroughSandboxTwoPhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_staging, "uniqId") instanceof DataStagingManagerThroughSandboxTwoPhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_interactive, "uniqId") instanceof DataStagingManagerThroughSandboxTwoPhase);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_simple, "uniqId") instanceof DataStagingManagerDummy);
    }

    @Test
    public void streamable() throws Exception {
        final JobControlAdaptor adaptor = m_mockery.mock(StreamableJobAdaptor.class, "streamable");
        assertTrue(DataStagingManagerFactory.create(adaptor) instanceof DataStagingManagerDummy);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_staging, "uniqId") instanceof DataStagingManagerThroughStream);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_interactive, "uniqId") instanceof DataStagingManagerDummy);
        assertTrue(DataStagingManagerFactory.create(adaptor, m_desc_simple, "uniqId") instanceof DataStagingManagerDummy);
    }

    @Test
    public void dummy() throws Exception {
        final JobControlAdaptor adaptor = m_mockery.mock(StagingJobAdaptor.class, "dummy");
        assertTrue(DataStagingManagerFactory.create(adaptor) instanceof DataStagingManagerDummy);
    }

}
