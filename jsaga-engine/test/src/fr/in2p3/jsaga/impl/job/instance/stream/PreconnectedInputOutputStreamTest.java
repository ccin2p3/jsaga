package fr.in2p3.jsaga.impl.job.instance.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.job.Job;
import org.ogf.saga.task.State;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetter;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;

public class PreconnectedInputOutputStreamTest {
    private static Mockery m_mockery;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @BeforeClass
    public static void setUp() {
        m_mockery = new Mockery() {{
            // multithread support for run()
            setThreadingPolicy(new Synchroniser());
        }};
    }

    // Not supported when RUNNING for each operation
    @Test
    public void stdInputRunning() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-running");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.RUNNING));
        }});
        PostconnectedStdinOutputStream psos = new PostconnectedStdinOutputStream(job);
        exception.expect(IOException.class);
        this.testOutputStream(psos);
    }
    
    @Test
    public void stdInputRunningInteractive() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-running-interactive");
        final JobIOGetterInteractive getter = m_mockery.mock(JobIOGetterInteractive.class);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.RUNNING));
            allowing(getter).getStdin(); will(returnValue(output));
        }});
        PostconnectedStdinOutputStream psos = new PostconnectedStdinOutputStream(job);
        psos.openJobIOHandler(getter);
        exception.expect(IOException.class);
        this.testOutputStream(psos);
        output.close();
    }

    
    // JOB finished
    @Test(expected=DoesNotExistException.class)
    public void stdInputEnded() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-ended");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
        }});
        new PostconnectedStdinOutputStream(job);
    }
    
    @Test
    public void stdInputNew() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-new");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.NEW));
        }});
        this.testOutputStream(new PostconnectedStdinOutputStream(job));
    }

    
    // OUTPUTS
    
    @Test
    public void stdOutputNew() throws Exception {
        this.testOutput(State.NEW, "stdout");
    }
    
    @Test
    public void stdOutputDone() throws Exception {
        this.testOutput(State.DONE, "stdout");
    }
    
    @Test
    public void stdOutputRunning() throws Exception {
        this.testOutput(State.RUNNING, "stdout");
    }
    
    @Test
    public void stdErrNew() throws Exception {
        this.testOutput(State.NEW, "stderr");
    }
    
    @Test
    public void stdErrDone() throws Exception {
        this.testOutput(State.DONE, "stderr");
    }
    
    @Test
    public void stdErrRunning() throws Exception {
        this.testOutput(State.RUNNING, "stderr");
    }

    // private
    private void testOutput(final State state, String type) throws Exception {
        final Job job = m_mockery.mock(Job.class, "job" + type + state.name());
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(state));
        }});
        if ("stderr".equals(type)) {
            this.testOutputStream(new PreconnectedStderrInputStream(job).getOutputStreamContainer());
        } else {
            this.testOutputStream(new PreconnectedStdoutInputStream(job).getOutputStreamContainer());
        }
    }
    
    private void testOutputStream(OutputStream os) throws Exception {
        os.write('1');
        os.write(".2".getBytes());
        os.write(".3 and the rest is not written".getBytes(), 0, 2);
        os.flush();
        os.close();
    }
}
