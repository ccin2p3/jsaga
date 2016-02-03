package fr.in2p3.jsaga.impl.resource.manager;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import fr.in2p3.jsaga.adaptor.resource.StorageResourceAdaptor;

public class ResourceManagerImplTest {

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
    public void listResourcesTemplates() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final ComputeResourceAdaptor adaptor;
        final String[] resources = {"res0","res1"};
        final String[] templates = {"temp0","temp1"};
        
        adaptor = m_mockery.mock(ComputeResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).listComputeResources(); will(returnValue(resources));
            allowing(adaptor).listComputeTemplates(); will(returnValue(templates));
        }});
        ResourceManagerImpl rm = new ResourceManagerImpl(m_session, m_url, adaptor);
        assertEquals(2, rm.listResources(Type.COMPUTE).size());
        assertEquals(2, rm.listResources(null).size());
    }

    @Test @Ignore("this test does not work: is not an interface")
    // TODO fix this test
    public void listComputeAndStorageResources() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final ResourceAdaptor adaptor;
        final String[] compute_resources = new String[2];
        compute_resources[0] = "cres0";
        compute_resources[1] = "cres1";
        final String[] storage_resources = new String[2];
        storage_resources[0] = "sres0";
        storage_resources[1] = "sres1";
        
        adaptor = m_mockery.mock(ComputeAndStorageResourceAdaptor.class);
        m_mockery.checking(new Expectations() {{
            ((ComputeResourceAdaptor) allowing(adaptor)).listComputeResources(); will(returnValue(compute_resources));
            ((StorageResourceAdaptor) allowing(adaptor)).listStorageResources(); will(returnValue(storage_resources));
        }});
        ResourceManagerImpl rm = new ResourceManagerImpl(m_session, m_url, adaptor);
        assertEquals(2, rm.listResources(Type.COMPUTE).size());
        assertEquals(2, rm.listResources(Type.STORAGE).size());
        assertEquals(4, rm.listResources(null).size());
    }

    @Test(expected = NotImplementedException.class)
    public void listResourcesNotImplemented() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final NetworkResourceAdaptor adaptor;
        adaptor = m_mockery.mock(NetworkResourceAdaptor.class);
        new ResourceManagerImpl(m_session, m_url, adaptor).listResources(Type.COMPUTE);
    }
    
    @Test(expected = NotImplementedException.class)
    public void listTemplatesNotImplemented() throws TimeoutException, NoSuccessException, 
            NotImplementedException, AuthenticationFailedException, AuthorizationFailedException {
        final NetworkResourceAdaptor adaptor;
        adaptor = m_mockery.mock(NetworkResourceAdaptor.class);
        new ResourceManagerImpl(m_session, m_url, adaptor).listTemplates(Type.COMPUTE);
    }
    
    private abstract class ComputeAndStorageResourceAdaptor implements ComputeResourceAdaptor, StorageResourceAdaptor {};
}
