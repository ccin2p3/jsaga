package org.ogf.saga.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.assumeTrue;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.ResourceTask;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ResourceBaseTest
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr
* Date:   25 JAN 2016
* ***************************************************
* Description:                                      */

//@RunWith(junitparams.JUnitParamsRunner.class)
public abstract class ResourceBaseTest extends JSAGABaseTest {
	
    private Logger m_logger = Logger.getLogger(this.getClass());
    // defaults
    private static final String DEFAULT_DELAY_BEFORE_USE = "0";
    


    // configuration
    protected String DELAY_BEFORE_USE       = "delayBeforeUse";
    protected String ACQUIRE_TEMPLATE       = "acquireTemplate";
    protected String RECONFIGURE_TEMPLATE   = "reconfigureTemplate";

    protected List<String> m_templatesForAcquire;
    protected List<String> m_templatesForReconfigure;
    protected int m_delayBeforeUseInSeconds;
    
    protected URL m_resourcemanager;
    protected Session m_session;
    protected ResourceManager m_rm;
    protected Type m_type;
    
    protected Resource m_currentResource;

    protected ResourceBaseTest(String resourceprotocol, Type type) throws Exception {
        super();
        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(resourceprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
        m_type = type;
        String prefix = resourceprotocol + "." + m_type.name();
        m_delayBeforeUseInSeconds = Integer.parseInt(
                super.getOptionalProperty(prefix, DELAY_BEFORE_USE, DEFAULT_DELAY_BEFORE_USE));
        m_templatesForAcquire = super.getOptionalProperties(prefix, ACQUIRE_TEMPLATE);
        m_templatesForReconfigure = super.getOptionalProperties(prefix, RECONFIGURE_TEMPLATE);
    }

    @Before
    public void setUp() throws Exception {
        m_rm = ResourceFactory.createResourceManager(m_session, m_resourcemanager);
        m_currentResource = null;
    }
    

    @After 
    public void cleanUp() throws Exception {
        if (this.m_currentResource != null) {
            try {
                this.m_currentResource.release();
            } catch (Exception e) {
                // ignore
            }
        }
    }
    protected abstract Resource acquire(ResourceDescription rd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException;
    
    //////////////
    // templates
    //////////////
    @Test
    public void listTemplates() throws Exception {
        List<String> templates = m_rm.listTemplates(m_type);
        assertNotNull(templates);
        if (templates.size()>0) {
            System.out.println(templates.get(0));
            // Details of a template
            ResourceDescription rd = m_rm.getTemplate(templates.get(0));
            if (Type.COMPUTE.equals(m_type)) {
                assertTrue(rd instanceof ComputeDescription);
            } else if (Type.STORAGE.equals(m_type)) {
                assertTrue(rd instanceof StorageDescription);
            } else if (Type.NETWORK.equals(m_type)) {
                assertTrue(rd instanceof NetworkDescription);
            } else {
                fail("Unknown type:" + m_type.name());
            }
            this.dumpDescription(rd);
        }
    }
    
    ////////////
    // Templates
    ////////////
    @Test
    public void getTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        assumeTrue(m_templatesForAcquire.size() > 0);
        m_rm.getTemplate(m_templatesForAcquire.get(0));
    }

    @Test(expected = DoesNotExistException.class)
    public void unknownTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        assumeTrue(m_templatesForAcquire.size() > 0);
        String templateToTest = "thisTemplateDoesNotExists";
        // Take the first template and insert "thisTemplateDoesNotExists" just before the last ']'
        templateToTest = m_templatesForAcquire.get(0).replaceAll("]$", templateToTest + "]");
        m_rm.getTemplate(templateToTest);
    }

    ////////////
    // List resources
    ////////////

    @Test
    public void listResources() throws Exception {
        List<String> resources = m_rm.listResources(m_type);
        int count = 1;
        for (String resourceId: resources) {
            Resource resource;
            if (Type.NETWORK.equals(m_type)) {
                resource = m_rm.acquireNetwork(resourceId);
            } else if (Type.STORAGE.equals(m_type)) {
                resource = m_rm.acquireStorage(resourceId);
            } else if (Type.COMPUTE.equals(m_type)) {
                resource = m_rm.acquireCompute(resourceId);
            } else {
                throw new Exception("Type not supported: " + m_type.name());
            }
            this.dumpResource(resource);
            if (count++ == 10) {
                return;
            }
        }
        
    }
    
    ////////////
    // create+release
    ////////////
    @Test
    public void acquireNewAndRelease() throws Exception {
        m_currentResource = this.acquireResourceReadyNotForUse();
        this.dumpResource(m_currentResource);
        for (Context c: m_session.listContexts()) {
            m_logger.info("** Context :" + c.getAttribute(Context.TYPE));
            m_logger.debug(c.toString());
        }
    }

    @Test
    public void acquire2ResourcesInTheSameSessionAndRelease() throws Exception {
        m_currentResource = this.acquireResourceReadyNotForUse();
        Resource res2 = null;
        try {
            res2 = this.acquireResourceReadyNotForUse();
            for (Context c: m_session.listContexts()) {
                m_logger.info("** Context :" + c.getAttribute(Context.TYPE));
                m_logger.debug(c.toString());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (res2 != null) {
                try {res2.release();} catch (Exception e) {}
            }
        }
    }

    @Test
    public void acquireAndReconfigureAndRelease() throws Exception {
        assumeTrue(m_templatesForReconfigure.size()>0);
        m_currentResource = this.acquireResourceReadyNotForUse();
        m_currentResource = this.reconfResourceReady(0);
    }

    ////////
    // Utils
    ////////
    /*
     * instantiate and wait ACTIVE + wait delayBeforeUse
     */
    protected final Resource acquireResourceReadyForUse() throws Exception {
        return this.acquireResourceReady(m_delayBeforeUseInSeconds);
    }
    /*
     * instantiate and wait ACTIVE
     */
    protected final Resource acquireResourceReadyNotForUse() throws Exception {
        return this.acquireResourceReady(0);
    }
    
    private final Resource acquireResourceReady(int delay) throws Exception {
        return this.getResourceFromTemplatesReady(delay, m_templatesForAcquire);
    }
    
    private final Resource reconfResourceReady(int delay) throws Exception {
        return this.getResourceFromTemplatesReady(delay, m_templatesForReconfigure);
    }
    
    private final Resource getResourceFromTemplatesReady(int delay, List<String> templateList) throws Exception {
        ResourceDescription nd = ResourceFactory.createResourceDescription(m_type);
        if (templateList.size() > 0) {
            String[] templates = new String[templateList.size()];
            nd.setVectorAttribute(ResourceDescription.TEMPLATE, templateList.toArray(templates));
        }
        return this.acquireResourceFromDescReady(delay, nd);
    }
    
    protected  final Resource acquireResourceFromDescReadyForUse(ResourceDescription nd) throws Exception {
        return this.acquireResourceFromDescReady(m_delayBeforeUseInSeconds, nd);
    }
    
    private final Resource acquireResourceFromDescReady(int delay, ResourceDescription nd) throws Exception {
        Resource res = this.acquire(nd);
        res.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, res.getState());
        if (delay > 0) {
            Thread.sleep(delay*1000);
        }
        return res;
    }
    
    
    protected void dumpDescription(ResourceDescription rd) throws Exception {
        for (String a: rd.listAttributes()) {
            System.out.println("  * " + a + "=" + rd.getAttribute(a));
        }
    }
    
    protected void dumpResource(Resource resource) throws Exception {
        ResourceDescription rd = (ResourceDescription) resource.getDescription();
        assertNotNull(rd);
        System.out.println(resource.getId());
        this.dumpDescription(rd);
        // display status
        System.out.println("  * status=" + resource.getState().name() + " // " 
                + resource.getMetric(ResourceTask.RESOURCE_STATEDETAIL).getAttribute(Metric.VALUE));
        for (String access: resource.getAccess()) {
            System.out.println("  => " + access);
        }
    }
}
