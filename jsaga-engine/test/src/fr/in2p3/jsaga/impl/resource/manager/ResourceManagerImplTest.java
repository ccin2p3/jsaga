package fr.in2p3.jsaga.impl.resource.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

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
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.instance.Storage;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import fr.in2p3.jsaga.adaptor.resource.ResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.compute.ComputeResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.network.NetworkResourceAdaptor;
import fr.in2p3.jsaga.adaptor.resource.storage.StorageResourceAdaptor;
import fr.in2p3.jsaga.impl.resource.description.ComputeDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.NetworkDescriptionImpl;
import fr.in2p3.jsaga.impl.resource.description.StorageDescriptionImpl;

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

    @Test
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
    
    @Test
    public void getTemplate() throws Exception {
        final ResourceAdaptor adaptor;
        adaptor = m_mockery.mock(ResourceAdaptor.class);
        ResourceManagerImpl rm = new ResourceManagerImpl(m_session, m_url, adaptor);
        ResourceDescription rd;
        final Properties desc = new Properties();
        desc.put("attr", "value");
        
        // COMPUTE
        desc.put(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getTemplate(with(any(String.class))); will(returnValue(desc));
        }});
        rd = rm.getTemplate("[url]-[id]");
        assertTrue(rd instanceof ComputeDescriptionImpl);
        assertEquals("value", rd.getAttribute("attr"));

        // STORAGE
        desc.put(Resource.RESOURCE_TYPE, Type.STORAGE.name());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getTemplate(with(any(String.class))); will(returnValue(desc));
        }});
        rd = rm.getTemplate("[url]-[id]");
        assertTrue(rd instanceof StorageDescriptionImpl);
        assertEquals("value", rd.getAttribute("attr"));

        // NETWORK
        desc.put(Resource.RESOURCE_TYPE, Type.NETWORK.name());
        m_mockery.checking(new Expectations() {{
            allowing(adaptor).getTemplate(with(any(String.class))); will(returnValue(desc));
        }});
        rd = rm.getTemplate("[url]-[id]");
        assertTrue(rd instanceof NetworkDescriptionImpl);
        assertEquals("value", rd.getAttribute("attr"));
    }
    
    @Test
    public void acquire() throws Exception {
        final Properties desc = new Properties();
        desc.put("attr", "value");
        ResourceManagerImpl rm;
        
        // COMPUTE
        final ResourceAdaptor c_adaptor = m_mockery.mock(ComputeResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        m_mockery.checking(new Expectations() {{
            allowing(c_adaptor).getDescription(with(any(String.class))); will(returnValue(desc));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, c_adaptor);
        Compute server = rm.acquireCompute("[url]-[server1]");
        assertEquals("[url]-[server1]", server.getResourceId());
        assertEquals("value", server.getDescription().getAttribute("attr"));

        // STORAGE
        final ResourceAdaptor s_adaptor = m_mockery.mock(StorageResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.STORAGE.name());
        m_mockery.checking(new Expectations() {{
            allowing(s_adaptor).getDescription(with(any(String.class))); will(returnValue(desc));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, s_adaptor);
        Storage repo = rm.acquireStorage("[url]-[repo1]");
        assertEquals("[url]-[repo1]", repo.getResourceId());
        assertEquals("value", repo.getDescription().getAttribute("attr"));

        // NETWORK
        final ResourceAdaptor n_adaptor = m_mockery.mock(NetworkResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.NETWORK.name());
        m_mockery.checking(new Expectations() {{
            allowing(n_adaptor).getDescription(with(any(String.class))); will(returnValue(desc));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, n_adaptor);
        Network net = rm.acquireNetwork("[url]-[net1]");
        assertEquals("[url]-[net1]", net.getResourceId());
        assertEquals("value", net.getDescription().getAttribute("attr"));
    }

    @Test
    public void release() throws Exception {
        final Properties desc = new Properties();
        desc.put("attr", "value");
        ResourceManagerImpl rm;
        
        // COMPUTE
        final ComputeResourceAdaptor c_adaptor = m_mockery.mock(ComputeResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.COMPUTE.name());
        m_mockery.checking(new Expectations() {{
            allowing(c_adaptor).release(with(any(String.class)));
            allowing(c_adaptor).release(with(any(String.class)), with(any(Boolean.class)));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, c_adaptor);
        rm.releaseCompute("[url]-[server1]");
        rm.releaseCompute("[url]-[server1]", false);

        // STORAGE
        final ResourceAdaptor s_adaptor = m_mockery.mock(StorageResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.STORAGE.name());
        m_mockery.checking(new Expectations() {{
            allowing(s_adaptor).release(with(any(String.class)));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, s_adaptor);
        rm.releaseStorage("[url]-[repo1]");

        // NETWORK
        final ResourceAdaptor n_adaptor = m_mockery.mock(NetworkResourceAdaptor.class);
        desc.put(Resource.RESOURCE_TYPE, Type.NETWORK.name());
        m_mockery.checking(new Expectations() {{
            allowing(n_adaptor).release(with(any(String.class)));
        }});
        rm = new ResourceManagerImpl(m_session, m_url, n_adaptor);
        rm.releaseStorage("[url]-[net1]");
    }

    private interface ComputeAndStorageResourceAdaptor extends ComputeResourceAdaptor, StorageResourceAdaptor {};
}
