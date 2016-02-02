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


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    public OpenstackResourceAdaptorTest() throws Exception {
        super("openstack");
    }

    @Test
    public void flavors() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException, PermissionDeniedException, 
            IncorrectStateException {
        // List all templates
        List<String> templates = m_rm.listTemplates(Type.COMPUTE);
        assertTrue(templates.size()>0);
        // Details of a template
        ResourceDescription rd = m_rm.getTemplate(templates.get(0));
        assertTrue(rd instanceof ComputeDescription);
        ComputeDescription cd = (ComputeDescription)rd;
        assertTrue(cd.existsAttribute(ComputeDescription.MEMORY));
        System.out.println(cd.getAttribute(ComputeDescription.MEMORY));
    }
    
    @Test
    public void servers() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException, PermissionDeniedException, 
            IncorrectStateException {
        List<String> resources = m_rm.listResources(Type.COMPUTE);
        assertTrue(resources.size()>0);
        // Details of a resource
        Compute server = m_rm.acquireCompute(resources.get(0));
        assertNotNull(server.getDescription());
        System.out.println(server.getDescription());
    }
    
}
