package fr.in2p3.jsaga.impl.resource.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.resource.ResourceStatus;
import fr.in2p3.jsaga.adaptor.resource.compute.UnsecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;
import fr.in2p3.jsaga.impl.resource.task.IndividualResourceStatusPoller;
import fr.in2p3.jsaga.impl.resource.task.ResourceMonitorCallback;

public class ResourceImplTest {

    private static Session m_session;
    private static URL m_url;
    private Mockery m_mockery;
    private ComputeImpl m_server;
    IndividualResourceStatusPoller m_poller;
    final Properties m_desc = new Properties();
    
    @BeforeClass
    public static void init() throws BadParameterException, NoSuccessException {
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("scheme://dummy");
    }
    
    @Before
    public void setUp() throws Exception {
        m_mockery = new Mockery() {{
            // multithread support for start() stop()
            setThreadingPolicy(new Synchroniser());
        }};
        m_desc.put(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        m_desc.put(ComputeDescription.SIZE, "2");
        ResourceManagerImpl rm;
        
        final UnsecuredComputeResourceAdaptor ucra = m_mockery.mock(UnsecuredComputeResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(ucra).getDescription(with(any(String.class))); will(returnValue(m_desc));
            allowing(ucra).release(with(any(String.class)), with(any(Boolean.class))); will(returnValue(null));
            allowing(ucra).release(with(any(String.class))); will(returnValue(null));
            allowing(ucra).getAccess(with(any(String.class))); will(returnValue(new String[]{"url1","url2"}));
            allowing(ucra).acquireComputeResource(with(any(Properties.class))); will(returnValue("[url]-[id]"));
            allowing(ucra).getResourceStatus(with(any(String.class))); will(returnValue(new DummyResourceStatus()));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, ucra);
        m_server = new ComputeImpl(m_session, rm, ucra, "[url]-[id]");
        m_poller = new IndividualResourceStatusPoller(ucra);
        //with secured adaptor
        /* this test does not work because SecuredResource ...
        final SecuredResource sr = new SecuredResource("[url]-[id]", "SSH");
        final SecuredComputeResourceAdaptor scra = m_mockery.mock(SecuredComputeResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(scra).getDescription(with(any(String.class))); will(returnValue(desc));
            allowing(scra).release(with(any(String.class)), with(any(Boolean.class))); will(returnValue(null));
            allowing(scra).release(with(any(String.class))); will(returnValue(null));
            allowing(scra).getAccess(with(any(String.class))); will(returnValue(new String[]{"url1","url2"}));
            allowing(scra).acquireComputeResource(with(any(Properties.class))); will(returnValue(null));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, scra);
        c = new ComputeImpl(m_session, rm, scra, "[url]-[id]");

        // reconfigure
        c.reconfigure(description);
        assertEquals("3", c.getDescription().getAttribute(ComputeDescription.SIZE));
        */
    }

    @Test
    public void properties() throws Exception {
        assertEquals("[url]-[id]", m_server.getResourceId());
        assertEquals(Type.COMPUTE, m_server.getType());
        assertEquals(2, m_server.getAccess().length);
        assertEquals("url1", m_server.getAccess()[0]);
        assertEquals("url2", m_server.getAccess()[1]);
    }
    
    @Test
    public void release() throws Exception {
        m_server.release(false);
        m_server.release();
    }

    @Test
    public void description() throws Exception {
        // get description
        assertEquals("2", m_server.getDescription().getAttribute(ComputeDescription.SIZE));
        
        // alter m_desc Properties
        m_desc.put(ComputeDescription.SIZE, "3");

        // new description with these new Properties
        ComputeDescription newDescription = m_server.createDescription(m_desc);
        assertEquals("3", newDescription.getAttribute(ComputeDescription.SIZE));
        
        // reconfigure with this new description
        m_server.reconfigure(newDescription);
        assertEquals("3", m_server.getDescription().getAttribute(ComputeDescription.SIZE));
    }
    
    @Test
    public void state() throws Exception {
        // Just test setState()
        m_server.setState(State.ACTIVE, "RUNNING");
        assertSame(State.ACTIVE, m_server.getState());
        // after delay, the adaptor should be requested getResourceStatus()
        // see DummyResourceStatus which returns State.FINAL
        Thread.sleep(7000);
        assertSame(State.FINAL, m_server.getState());
        // waitfor
        m_server.setState(State.ACTIVE, "RUNNING");
        m_server.waitFor(2, State.FINAL);
        try {
            m_server.waitFor(2, State.CLOSED);
        } catch (TimeoutException te) {
            // OK
        }
        
        // listener
        m_server.startListening();
        m_server.stopListening();
        
    }
    
    @Test
    public void individualResourceStatusPoller() throws Exception {
        final ResourceMonitorCallback callback = m_mockery.mock(ResourceMonitorCallback.class);
        m_mockery.checking(new Expectations() {{
            allowing(callback).setState(with(any(State.class)), with(any(String.class))); will(returnValue(null));
        }});
        m_poller.subscribeResource("id", callback);
        m_poller.run();
        m_poller.unsubscribeResource("id");
    }
    
    private class DummyResourceStatus extends ResourceStatus {

        public DummyResourceStatus() {
            super(null, null);
        }

        @Override
        public State getSagaState() {
            return State.FINAL;
        }

        @Override
        protected String getModel() {
            return "DUMMY";
        }
        
    }
}
