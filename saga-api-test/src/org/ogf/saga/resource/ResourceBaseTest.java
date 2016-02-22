package org.ogf.saga.resource;

import java.util.List;

import junitparams.Parameters;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.NetworkDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.description.StorageDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.instance.Network;
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.resource.instance.Storage;
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

@RunWith(junitparams.JUnitParamsRunner.class)
public abstract class ResourceBaseTest extends JSAGABaseTest {
	
    private Logger m_logger = Logger.getLogger(this.getClass());

    protected URL m_resourcemanager;
    protected Session m_session;
    protected ResourceManager m_rm;

    protected ResourceBaseTest(String resourceprotocol) throws Exception {
        super();

        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(resourceprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
    }

    protected Object[] typeToBeTested() {
        return new Object[][] {
                {Type.COMPUTE},
                {Type.STORAGE},
                {Type.NETWORK}
        };
    }

    @Before
    public void setUp() throws NotImplementedException, BadParameterException, IncorrectURLException, 
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        m_rm = ResourceFactory.createResourceManager(m_session, m_resourcemanager);
    }
    

    private Object[] parametersForListTemplates() {
        return typeToBeTested();
    }

    @Test
    @Parameters()
    public void listTemplates(Type type) throws Exception {
        List<String> templates = m_rm.listTemplates(type);
        assertNotNull(templates);
        if (templates.size()>0) {
            System.out.println(templates.get(0));
            // Details of a template
            ResourceDescription rd = m_rm.getTemplate(templates.get(0));
            if (Type.COMPUTE.equals(type)) {
                assertTrue(rd instanceof ComputeDescription);
            } else if (Type.STORAGE.equals(type)) {
                assertTrue(rd instanceof StorageDescription);
            } else if (Type.NETWORK.equals(type)) {
                assertTrue(rd instanceof NetworkDescription);
            } else {
                fail("Unknown type:" + type.name());
            }
            this.dumpDescription(rd);
        }
    }
    
    ////////////
    // List resources
    ////////////
    private Object[] parametersForListResources() {
        return typeToBeTested();
    }

    @Test
    @Parameters
    public void listResources(Type type) throws Exception {
        List<String> resources = m_rm.listResources(type);
        int count = 1;
        for (String resourceId: resources) {
            Resource resource;
            if (Type.NETWORK.equals(type)) {
                resource = m_rm.acquireNetwork(resourceId);
            } else if (Type.STORAGE.equals(type)) {
                resource = m_rm.acquireStorage(resourceId);
            } else if (Type.COMPUTE.equals(type)) {
                resource = m_rm.acquireCompute(resourceId);
            } else {
                throw new Exception("Type not supported: " + type.name());
            }
            this.dumpResource(resource);
            if (count++ == 10) {
                return;
            }
        }
        
    }
    ////////
    // Utils
    ////////
    
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
