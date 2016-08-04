package fr.in2p3.jsaga.impl.job.instance.stream;

import java.io.IOException;
import java.io.PipedOutputStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import fr.in2p3.jsaga.adaptor.job.control.interactive.JobIOSetter;

public class PipedStdoutStderrTest {
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

    @Test
    public void pipedStdoutOK() throws Exception {
        final JobIOSetter setter = m_mockery.mock(JobIOSetter.class, "out-ok");
        m_mockery.checking(new Expectations() {{
            allowing(setter).setStdout(with(any(PipedOutputStream.class))); will(returnValue(null));
        }});
        PipedStdout pout = new  PipedStdout(setter);
        pout.read();
        pout.read(new byte[1024]);
        pout.read(new byte[1024], 0, 2);
        pout.close();
    }

    @Test
    public void pipedStdoutERROR() throws Exception {
        final JobIOSetter setter = m_mockery.mock(JobIOSetter.class, "out-error");
        m_mockery.checking(new Expectations() {{
            allowing(setter).setStdout(with(any(PipedOutputStream.class))); will(throwException(new Exception()));
        }});
        PipedStdout pout = new  PipedStdout(setter);
        
        exception.expect(IOException.class);
        pout.read();
        pout.read(new byte[1024]);
        pout.read(new byte[1024], 0, 2);
        pout.close();
    }

    @Test
    public void pipedStderrOK() throws Exception {
        final JobIOSetter setter = m_mockery.mock(JobIOSetter.class, "err-ok");
        m_mockery.checking(new Expectations() {{
            allowing(setter).setStderr(with(any(PipedOutputStream.class))); will(returnValue(null));
        }});
        PipedStderr perr = new  PipedStderr(setter);
        perr.read();
        perr.read(new byte[1024]);
        perr.read(new byte[1024], 0, 2);
        perr.close();
    }

    @Test
    public void pipedStderrERROR() throws Exception {
        final JobIOSetter setter = m_mockery.mock(JobIOSetter.class, "err-error");
        m_mockery.checking(new Expectations() {{
            allowing(setter).setStderr(with(any(PipedOutputStream.class))); will(throwException(new Exception()));
        }});
        PipedStderr perr = new  PipedStderr(setter);
        
        exception.expect(IOException.class);
        perr.read();
        perr.read(new byte[1024]);
        perr.read(new byte[1024], 0, 2);
        perr.close();
    }
}
