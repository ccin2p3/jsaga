package fr.in2p3.jsaga.impl.resource.task;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Test;

public class ResourceStatusPollerTaskTest {

    private Mockery m_mockery;
    
    @Before
    public void setUp() {
        m_mockery = new Mockery() {{
            // multithread support for start() stop()
            setThreadingPolicy(new Synchroniser());
        }};
    }

    @Test
    public void run() throws Exception {
        final Runnable task = m_mockery.mock(Runnable.class);
        m_mockery.checking(new Expectations() {{
            allowing(task).run(); will(returnValue(null));
        }});
        ResourceStatusPollerTask pollerTask = new ResourceStatusPollerTask(task);
        // directly run
        pollerTask.run();
        // use timer
        pollerTask.start();
        pollerTask.stop();
    }
}
