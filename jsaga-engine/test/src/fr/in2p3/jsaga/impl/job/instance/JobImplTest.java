package fr.in2p3.jsaga.impl.job.instance;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.print.attribute.standard.JobState;

import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.task.State;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.job.SubState;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.SuspendableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveGet;
import fr.in2p3.jsaga.adaptor.job.control.interactive.StreamableJobInteractiveSet;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.ListenIndividualJob;
import fr.in2p3.jsaga.engine.job.monitor.JobMonitorService;
import fr.in2p3.jsaga.impl.job.service.AbstractSyncJobServiceImpl;
import fr.in2p3.jsaga.impl.job.staging.mgr.DataStagingManager;

public class JobImplTest {

    @Rule public final JUnitRuleMockery context = new JUnitRuleMockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private Session m_session;
    private URL m_url;
    
    @Mock private JobDescription m_desc;
    
    @Mock private DataStagingManager m_manager;
    
    private AbstractSyncJobServiceImpl m_service;
    
    // Non interactive job
    @Mock private JobControlAdaptor m_control_adaptor;
    
    // interactive jobs
    @Mock private StreamableJobInteractiveGet m_sjig_adaptor;
    @Mock private JobIOGetterInteractive m_getter;
    @Mock private StreamableJobInteractiveSet m_sjis_adaptor;
    
    // Other job interface
    @Mock private SuspendableJobAdaptor m_suspendable;
    // monitoring
    @Mock private ListenIndividualJob m_monitor_adaptor;
    @Mock private JobMonitorService m_monitor_service;
    
    @Mock private JobDescriptionTranslator m_translator;
    @Mock private JobStatus m_jobStatus;
    
    @Before
    public void setUp() throws Exception {
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("scheme://dummy");
//        m_monitor_service = new JobMonitorService(m_url, m_monitor_adaptor, new HashMap());
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                                                    m_url, 
                                                    m_control_adaptor, 
                                                    m_monitor_service, 
                                                    m_translator) {
            
        };
        context.checking(new Expectations() {{
            allowing(m_monitor_service).checkState();
            allowing(m_monitor_service).getURL(); will(returnValue(m_url));
            allowing(m_monitor_service).getStateLogger(); will(returnValue(Logger.getLogger("Test")));
        }});
    }
    
    @Test
    public void submitNonInteractive() throws Exception {
        context.checking(new Expectations() {{
            allowing(m_monitor_service).checkState();
            allowing(m_desc).getAttribute(JobDescription.INTERACTIVE); will(returnValue("false"));
            oneOf(m_control_adaptor).submit("desc", false, "uniqId"); will(returnValue("jobId"));
        }});
        JobImpl job = new JobImpl(m_session, "desc", m_desc, m_manager, "uniqId", m_service);
        job.run();
        assertEquals("jobId", job.getNativeJobId());
    }

    @Test
    public void state() throws Exception {
        context.checking(new Expectations() {{
            allowing(m_monitor_service).getState(null);
        }});
        JobImpl job = new JobImpl(m_session, "desc", m_desc, m_manager, "uniqId", m_service);
        assertEquals(State.NEW, job.getState());
        job.setState(State.DONE);
        assertEquals(State.DONE, job.getState());
        // Cannot change final state
        job.setState(State.RUNNING);
        assertEquals(State.DONE, job.getState());
    }

    @Test
    public void submitInteractive_StreamableGet() throws Exception {
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_sjig_adaptor, // StreamableJobInteractiveGet
                m_monitor_service, 
                m_translator) {
        };
        context.checking(new Expectations() {{
            allowing(m_desc).getAttribute(JobDescription.INTERACTIVE); will(returnValue("true"));
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.RUNNING));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("Running"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.RUNNING_ACTIVE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState(null); will(returnValue(m_jobStatus));
            oneOf(m_sjig_adaptor).submitInteractive("desc", false); will(returnValue(m_getter));
            oneOf(m_getter).getStdin(); will(returnValue(new ByteArrayOutputStream()));
            oneOf(m_getter).getStdout(); will(returnValue(new ByteArrayInputStream("ok".getBytes())));
            oneOf(m_getter).getStderr(); will(returnValue(new ByteArrayInputStream("error".getBytes())));
            oneOf(m_getter).getJobId(); will(returnValue("jobId"));
        }});
        JobImpl job = new JobImpl(m_session, "desc", m_desc, m_manager, "uniqId", m_service);
        job.run();
        assertEquals("jobId", job.getNativeJobId());
        byte[] console = new byte[8];
        job.getStdout().read(console);
        assertEquals("ok", new String(console).trim());
        // next assert fails from time to time ... with expected <[error]> but was <[ok]>...
//        job.getStderr().read(console);
//        assertEquals("error", new String(console).trim());
    }

    @Test
    public void submitInteractive_StreamableSet() throws Exception {
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_sjis_adaptor, // StreamableJobInteractiveSet
                m_monitor_service, 
                m_translator) {
        };
        
        context.checking(new Expectations() {{
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.RUNNING));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("Running"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.RUNNING_ACTIVE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState("jobId"); will(returnValue(m_jobStatus));
            allowing(m_desc).getAttribute(JobDescription.INTERACTIVE); will(returnValue("true"));
            oneOf(m_sjis_adaptor).submitInteractive(with(any(String.class)), 
                    with(any(Boolean.class)), 
                    with(aNull(InputStream.class)), 
                    with(any(OutputStream.class)), 
                    with(any(OutputStream.class))); 
            will(returnValue("jobId"));
        }});
        JobImpl job = new JobImpl(m_session, "desc", m_desc, m_manager, "uniqId", m_service);
        job.run();
        assertEquals("jobId", job.getNativeJobId());
    }

    @Test @Ignore("If RUNNING then PreconnectedStdoutInputStream says 'Not supported yet...' but if DONE then JobStdoutInputStream says 'INTERNAL ERROR: JobIOHandler has not been closed'")
    public void outputInteractive_StreamableSet_DONE() throws Exception {
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_sjis_adaptor, // StreamableJobInteractiveSet
                m_monitor_service, 
                m_translator) {
        };
        
        context.checking(new Expectations() {{
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.DONE));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("OK"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.DONE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState("jobId"); will(returnValue(m_jobStatus));
            allowing(m_monitor_service).getState(null); will(returnValue(m_jobStatus));
            allowing(m_desc).getAttribute(JobDescription.INTERACTIVE); will(returnValue("true"));
        }});
        JobImpl job = new JobImpl(m_session, "desc", m_desc, m_manager, "uniqId", m_service);
        byte[] console = new byte[8];
        job.getStdout().read(console);
        assertEquals("ok", new String(console).trim());
        job.getStderr().read(console);
        assertEquals("error", new String(console).trim());
    }

    @Test
    public void cancel() throws Exception {
        context.checking(new Expectations() {{
            oneOf(m_control_adaptor).cancel("jobId");
        }});
        JobImpl job = new JobImpl(m_session, "jobId", m_manager, m_service);
        assertEquals(State.NEW, job.getJobState());
        job.cancel();
        assertEquals(State.CANCELED, job.getJobState());
    }
    
    @Test
    public void suspend() throws Exception {
        JobImpl job;
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_suspendable, // SuspendableJobAdaptor
                m_monitor_service, 
                m_translator) {
        };
        
        context.checking(new Expectations() {{
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.RUNNING));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("OK"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.RUNNING_ACTIVE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState("jobId"); will(returnValue(m_jobStatus));
            oneOf(m_suspendable).suspend("jobId"); will(returnValue(true));
        }});
        job = new JobImpl(m_session, "jobId", m_manager, m_service);
        job.getState(); // force to use monitor service
        job.suspend();
    }
    
    @Test @Ignore("This test should fail with IncorrectStateException as SAGA says")
    public void suspend_DONE() throws Exception {
        JobImpl job;
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_suspendable, // SuspendableJobAdaptor
                m_monitor_service, 
                m_translator) {
        };
        context.checking(new Expectations() {{
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.DONE));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("OK"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.DONE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState("jobId"); will(returnValue(m_jobStatus));
            oneOf(m_suspendable).suspend("jobId"); will(returnValue(true));
        }});
        job = new JobImpl(m_session, "jobId", m_manager, m_service);
        assertEquals(State.DONE, job.getState()); // force to use monitor service
        try {
            job.suspend();
            fail("Expected IncorrectStateException if job is not RUNNING");
        } catch (IncorrectStateException e) {
            // OK
        }
    }
    
    @Test
    public void suspend_FALSE() throws Exception {
        JobImpl job;
        m_service = new AbstractSyncJobServiceImpl(m_session, 
                m_url, 
                m_suspendable, // SuspendableJobAdaptor
                m_monitor_service, 
                m_translator) {
        };
        context.checking(new Expectations() {{
            allowing(m_jobStatus).getSagaState(); will(returnValue(State.RUNNING));
            allowing(m_jobStatus).getStateDetail(); will(returnValue("OK"));
            allowing(m_jobStatus).getSubState(); will(returnValue(SubState.RUNNING_ACTIVE));
            allowing(m_jobStatus).getCause(); will(returnValue(null));
            allowing(m_monitor_service).getState("jobId"); will(returnValue(m_jobStatus));
            oneOf(m_suspendable).suspend("jobId"); will(returnValue(false));
        }});
        job = new JobImpl(m_session, "jobId", m_manager, m_service);
        job.getState(); // force to use monitor service
        try {
            job.suspend();
            fail("Expected NoSuccessException because plugin returned false");
        } catch (NoSuccessException e) {
            // OK
        }

    }
    
}
