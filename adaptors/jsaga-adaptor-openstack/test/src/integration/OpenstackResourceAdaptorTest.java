package integration;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.ResourceBaseTest;
import org.ogf.saga.resource.Type;


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack");
    }

    @Override
    @Test @Ignore
    public void listStorageTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listNetworkTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.NETWORK));
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listNetworkResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.NETWORK));
    }
    
}
