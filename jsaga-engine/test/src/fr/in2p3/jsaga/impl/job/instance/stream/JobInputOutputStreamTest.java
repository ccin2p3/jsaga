package fr.in2p3.jsaga.impl.job.instance.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.job.Job;
import org.ogf.saga.task.State;

import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetter;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOGetterInteractive;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;

public class JobInputOutputStreamTest {
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

    @Test(expected=DoesNotExistException.class)
    public void stdInputEnded() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-ended");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
        }});
        new JobStdinOutputStream(job);
    }
    
    @Test
    public void stdInputNew() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-new");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.NEW));
        }});
        JobStdinOutputStream jsos = new JobStdinOutputStream(job);
        jsos.write('1');
        jsos.write(".2".getBytes());
        jsos.write(".3 and the rest is not written".getBytes(), 0, 2);
        jsos.flush();
        jsos.close();
    }

    @Test
    public void stdInputRunningInteractive() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdin-running");
        final JobIOGetterInteractive getter = m_mockery.mock(JobIOGetterInteractive.class);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.RUNNING));
            allowing(getter).getStdin(); will(returnValue(output));
        }});
        JobStdinOutputStream jsos = new JobStdinOutputStream(job);
        jsos.openJobIOHandler(getter);
        jsos.write('1');
        jsos.write(".2".getBytes());
        jsos.write(".3 and the rest is not written".getBytes(), 0, 2);
        jsos.flush();
        jsos.close();
        assertEquals("1.2.3", output.toString());
        output.close();
    }

    @Test(expected=DoesNotExistException.class)
    public void stdOutputNew() throws Exception {
        final Job job = m_mockery.mock(Job.class, "stdout-new");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.NEW));
        }});
        new JobStdoutInputStream(job, null); // JobIOHandler is not used...
    }
    
    @Test
    public void stdOutputGetterDone() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stdout-getter");
        final JobIOGetter handler = m_mockery.mock(JobIOGetter.class, "handler-stdout-getter");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
            allowing(handler).getStdout(); will(returnValue(new ByteArrayInputStream("OUTPUT_STRING".getBytes())));
        }});
        JobStdoutInputStream jsis = new JobStdoutInputStream(job, handler);
        assertFalse(jsis.markSupported());

        // must close IOHandler before doing all this
        exception.expect(IOException.class);
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
        
        exception.none();
        // close IOHandler
        jsis.closeJobIOHandler();
        // now all this works
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }
    
    @Test
    public void stdOutputGetterRunning() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stdout-getter-running");
        final JobIOGetter handler = m_mockery.mock(JobIOGetter.class, "handler-stdout-getter-running");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.RUNNING));
            allowing(handler).getStdout(); will(returnValue(new ByteArrayInputStream("OUTPUT_STRING".getBytes())));
        }});
        JobStdoutInputStream jsis = new JobStdoutInputStream(job, handler);
        assertFalse(jsis.markSupported());

        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }
    
    @Test
    public void stdOutputSetterDone() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stdout-setter");
        final JobIOSetter handler = m_mockery.mock(JobIOSetter.class, "handler-stdout-setter");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
            allowing(handler).setStdout(with(any(PipedOutputStream.class))); will(returnValue(null));
        }});
        JobStdoutInputStream jsis = new JobStdoutInputStream(job, handler);
        assertFalse(jsis.markSupported());

        // must close IOHandler before doing all this
        exception.expect(IOException.class);
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
        
        exception.none();
        // close IOHandler
        jsis.closeJobIOHandler();
        // now all this works
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }

    @Test
    public void stdErrGetterDone() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stderr-getter");
        final JobIOGetter handler = m_mockery.mock(JobIOGetter.class, "handler-stderr-getter");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
            allowing(handler).getStderr(); will(returnValue(new ByteArrayInputStream("ERROR_STRING".getBytes())));
        }});
        JobStderrInputStream jsis = new JobStderrInputStream(job, handler);
        assertFalse(jsis.markSupported());

        // must close IOHandler before doing all this
        exception.expect(IOException.class);
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
        
        exception.none();
        // close IOHandler
        jsis.closeJobIOHandler();
        // now all this works
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }
    
    @Test
    public void stdErrGetterRunning() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stderr-getter-running");
        final JobIOGetter handler = m_mockery.mock(JobIOGetter.class, "handler-stderr-getter-running");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.RUNNING));
            allowing(handler).getStderr(); will(returnValue(new ByteArrayInputStream("ERROR_STRING".getBytes())));
        }});
        JobStderrInputStream jsis = new JobStderrInputStream(job, handler);
        assertFalse(jsis.markSupported());

        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }
    
    @Test
    public void stdErrSetterDone() throws Exception {
        byte[] b = new byte[4];
        final Job job = m_mockery.mock(Job.class, "job-stderr-setter");
        final JobIOSetter handler = m_mockery.mock(JobIOSetter.class, "handler-stderr-setter");
        m_mockery.checking(new Expectations() {{
            allowing(job).getState(); will(returnValue(State.DONE));
            allowing(handler).setStderr(with(any(PipedOutputStream.class))); will(returnValue(null));
        }});
        JobStderrInputStream jsis = new JobStderrInputStream(job, handler);
        assertFalse(jsis.markSupported());

        // must close IOHandler before doing all this
        exception.expect(IOException.class);
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
        
        exception.none();
        // close IOHandler
        jsis.closeJobIOHandler();
        // now all this works
        assertTrue(jsis.read() >= 0);
        assertEquals(4, jsis.read(b, 0, 4));
        assertEquals(4, jsis.read(b));
        jsis.skip(1);
        jsis.close();
    }
}
