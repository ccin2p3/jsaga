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
    private final static String[] m_templatesForAcquire = new String[]{
            "[DUMMY_URL]-[nova/images/official-centosCC-7x-x86_64]",
            "[DUMMY_URL]-[nova/flavors/m1.small]"
    };
    private final static String[] m_templatesForReconfigure = new String[]{
        "[DUMMY_URL]-[nova/images/official-ubuntu-14.04-x86_64]",
        "[DUMMY_URL]-[nova/flavors/m1.small]"
    };

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack", m_templatesForAcquire, m_templatesForReconfigure);
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listStorageTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.STORAGE));
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listNetworkTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.NETWORK));
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listStorageResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.STORAGE));
    }

    @Override
    @Test(expected=NotImplementedException.class)
    public void listNetworkResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.NETWORK));
    }

    @Override
    @Ignore("SSHD is not available on second boot")
    @Test
    public void launchAndReconfigureDeleteVM() throws Exception {
    }
}
