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
    public void images() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException, PermissionDeniedException, 
            IncorrectStateException {
        // List all templates
        List<String> templates = m_rm.listTemplates(Type.COMPUTE);
        assertTrue(templates.size()>0);
        System.out.println(templates.get(0));
        // Details of a template
        ResourceDescription rd = m_rm.getTemplate(templates.get(0));
        assertTrue(rd instanceof ComputeDescription);
        ComputeDescription cd = (ComputeDescription)rd;
        assertTrue(cd.existsAttribute(ComputeDescription.MACHINE_OS));
        System.out.println(cd.getAttribute(ComputeDescription.MACHINE_OS));
    }
    
    @Test
    public void servers() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException, PermissionDeniedException, 
            IncorrectStateException {
        List<String> resources = m_rm.listResources(Type.COMPUTE);
        assertTrue(resources.size()>0);
        // Details of a resource
//        Compute server = m_rm.acquireCompute(resources.get(0));
//        System.out.println(resources.get(0));
//        ResourceDescription cd = server.getDescription();
//        assertNotNull(cd);
//        for (String a: cd.listAttributes()) {
//            System.out.println(a + "=" + cd.getAttribute(a));
//        }
//        // RV
//        server = m_rm.acquireCompute("[dummy]-[http://ccnova.in2p3.fr:8774/v2/0223bc1968bc4e46932c5d87012aaf14/servers/9a9313ec-c987-42c0-846d-f9e0e6ca364e]");
//        cd = server.getDescription();
//        assertNotNull(cd);
//        for (String a: cd.listAttributes()) {
//            System.out.println(a + "=" + cd.getAttribute(a));
//        }
//        assertNotNull(server.getDescription());
        for (String serverId: resources) {
            Compute server = m_rm.acquireCompute(serverId);
            ResourceDescription rd = server.getDescription();
            assertNotNull(rd);
            System.out.println(serverId);
            for (String a: rd.listAttributes()) {
                System.out.println("  * " + a + "=" + rd.getAttribute(a));
            }
            
        }
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
