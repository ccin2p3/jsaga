package integration;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.ResourceBaseTest;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.State;


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack");
    }

    @Test
    public void imagesAndFlavors() throws Exception {
        // List all templates
        List<String> templates = m_rm.listTemplates(Type.COMPUTE);
        assertTrue(templates.size()>0);
        System.out.println(templates.get(0));
        // Details of a template
        ResourceDescription rd = m_rm.getTemplate(templates.get(0));
        assertTrue(rd instanceof ComputeDescription);
        this.dumpDescription(rd);
    }
    
    @Test
    public void launchAndDeleteVM() throws Exception {
        ResourceDescription cd = (ResourceDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[]{
                "[DUMMY_URL]-[nova/images/official-centosCC-7x-x86_64]",
                "[DUMMY_URL]-[nova/flavors/m1.small]"
        };
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, templates);
        Compute server = m_rm.acquireCompute((ComputeDescription) cd);
        server.waitFor(120);
        ResourceDescription rd = server.getDescription();
        assertNotNull(rd);
        System.out.println(server.getId());
        this.dumpDescription(rd);
        m_rm.releaseCompute(server.getId());
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


}
