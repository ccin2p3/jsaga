package fr.in2p3.jsaga.adaptor.openstack;

import java.util.Properties;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ResourceDescription;

import fr.in2p3.jsaga.adaptor.openstack.resource.OpenstackResourceAdaptor;
import fr.in2p3.jsaga.adaptor.openstack.security.OpenstackSecurityCredential;

public class OpenstackAdaptorTest extends JSAGABaseTest {

    private OpenstackResourceAdaptor adaptor;
    
    public OpenstackAdaptorTest() throws Exception {
        super();
    }

    @Before
    public void connect() throws NotImplementedException, AuthenticationFailedException, 
                AuthorizationFailedException, IncorrectURLException, BadParameterException, 
                TimeoutException, NoSuccessException {
        adaptor = new OpenstackResourceAdaptor();
        adaptor.setSecurityCredential(new OpenstackSecurityCredential("schwarz", "changeit", "ccin2p3"));
        adaptor.connect(null, "cckeystone.in2p3.fr", 5000, "/v2.0/", null);
    }
    
    @After
    public void disconnect() throws NoSuccessException {
        adaptor.disconnect();
    }
    
    @Test
    public void listVMs() throws TimeoutException, NoSuccessException {
        for (String server: adaptor.listResources()) {
            System.out.println("- " + server);
        }
    }
    
    @Test
    public void listFlavors() throws TimeoutException, NoSuccessException {
        for (String flavor: adaptor.listTemplates()) {
            System.out.println("- " + flavor);
        }
    }
    
    @Test
    public void launchVMWithOS() {
        Properties desc = new Properties();
        UUID id = UUID.randomUUID();
        desc.setProperty(OpenstackResourceAdaptor.DESC_IMAGE, "1b30ac1b-0dc6-48b0-9290-07b21b6ef575"); // CentOS
        desc.setProperty(OpenstackResourceAdaptor.DESC_FLAVOR, "41"); // m1.small.2
        desc.setProperty(OpenstackResourceAdaptor.DESC_NAME, "schwarz_jsaga_" + id);
        adaptor.acquire(desc);
    }
    
    @Test
    public void launchVMWithSAGA() throws NotImplementedException, BadParameterException, NoSuccessException {
        ResourceDescription desc = ResourceFactory.createResourceDescription(Type.COMPUTE);
        
//        adaptor.acquire(desc);
    }
    
}
