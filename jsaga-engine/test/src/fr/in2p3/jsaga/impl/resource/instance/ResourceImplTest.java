package fr.in2p3.jsaga.impl.resource.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.resource.compute.UnsecuredComputeResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.manager.ResourceManagerImpl;

public class ResourceImplTest {

    private static Session m_session;
    private static URL m_url;
    private Mockery m_mockery;
    
    @BeforeClass
    public static void init() throws BadParameterException, NoSuccessException {
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("scheme://dummy");
    }
    
    @Before
    public void setUp() {
        m_mockery = new Mockery();
    }

    @Test
    public void Compute() throws Exception {
        final Properties desc = new Properties();
        desc.put(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        desc.put(ComputeDescription.SIZE, "2");
        ResourceManagerImpl rm;
        ComputeImpl c;
        
        final UnsecuredComputeResourceAdaptor ucra = m_mockery.mock(UnsecuredComputeResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(ucra).getDescription(with(any(String.class))); will(returnValue(desc));
            allowing(ucra).release(with(any(String.class)), with(any(Boolean.class))); will(returnValue(null));
            allowing(ucra).release(with(any(String.class))); will(returnValue(null));
            allowing(ucra).getAccess(with(any(String.class))); will(returnValue(new String[]{"url1","url2"}));
            allowing(ucra).acquireComputeResource(desc); will(returnValue("[url]-[id]"));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, ucra);
        c = new ComputeImpl(m_session, rm, ucra, "[url]-[id]");
        
        // checks
        assertEquals("[url]-[id]", c.getId());
        assertEquals(Type.COMPUTE, c.getType());
        assertSame(rm, c.getManager());
        assertEquals(2, c.getAccess().length);
        assertEquals("url1", c.getAccess()[0]);
        assertEquals("url2", c.getAccess()[1]);
        
        // release drain
        c.release(false);

        // get description
        ComputeDescription description = c.getDescription();
        assertEquals("2", description.getAttribute(ComputeDescription.SIZE));
        
        // create description
        desc.put(ComputeDescription.SIZE, "3");
        description = c.createDescription(desc);
        assertEquals("3", description.getAttribute(ComputeDescription.SIZE));
        
        // reconfigure
        c.reconfigure(description);
        assertEquals("3", c.getDescription().getAttribute(ComputeDescription.SIZE));
        
        
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
}
