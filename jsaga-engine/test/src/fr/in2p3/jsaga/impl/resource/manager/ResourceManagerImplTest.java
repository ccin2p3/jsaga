package fr.in2p3.jsaga.impl.resource.manager;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.Type;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.resource.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.NetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;

public class ResourceManagerImplTest {

    private static Session m_session;
    private static URL m_url;
    private static Mockery m_mockery;
    
    @BeforeClass
    public static void init() throws BadParameterException, NoSuccessException {
        m_session = SessionFactory.createSession(false);
        m_url = URLFactory.createURL("scheme://dummy");
        m_mockery = new Mockery();
    }
    
    @Test
    public void listResources() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final ComputeResourceAdaptor adaptor;
        final String[] resources = new String[2];
        resources[0] = "res0";
        resources[1] = "res1";
        
        adaptor = m_mockery.mock(ComputeResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).listComputeResources(); will(returnValue(resources));
        }});
        ResourceManagerImpl rm = new ResourceManagerImpl(m_session, m_url, adaptor);
        assertEquals(2, rm.listResources(Type.COMPUTE).size());
        assertEquals(2, rm.listResources(null).size());
    }

    // TODO: test with 2 implements
    
    @Test(expected = NotImplementedException.class)
    public void listResourcesNotImplemented() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final NetworkResourceAdaptor adaptor;
        adaptor = m_mockery.mock(NetworkResourceAdaptor.class);
        new ResourceManagerImpl(m_session, m_url, adaptor).listResources(Type.COMPUTE);
    }
}
