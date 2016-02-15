package org.ogf.saga.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.JSAGABaseTest;
import org.ogf.saga.context.Context;
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
import org.ogf.saga.job.Job;
import org.ogf.saga.job.JobDescription;
import org.ogf.saga.job.JobFactory;
import org.ogf.saga.job.JobService;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Compute;
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

public abstract class ResourceBaseTest extends JSAGABaseTest {
	
    private Logger m_logger = Logger.getLogger(this.getClass());
    // configuration
    protected URL m_resourcemanager;
    protected Session m_session;
    protected ResourceManager m_rm;
    protected String[] m_templateListForAcquire;
    protected String[] m_templateListForReconfigure;
    
    protected ResourceBaseTest(String resourceprotocol, String[] templatesForAcquire, String[] templatesForReconfig) throws Exception {
        super();

        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(resourceprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
        m_templateListForAcquire = templatesForAcquire;
        m_templateListForReconfigure = templatesForReconfig;
    }

    @Before
    public void setUp() throws NotImplementedException, BadParameterException, IncorrectURLException, 
            AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        m_rm = ResourceFactory.createResourceManager(m_session, m_resourcemanager);
    }
    
    ////////////
    // Templates
    ////////////
    @Test(expected = DoesNotExistException.class)
    public void unknownTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        String templateToTest = "thisTemplateDoesNotExists";
        // Take the first template and insert "thisTemplateDoesNotExists" just before the last ']'
        templateToTest = m_templateListForAcquire[0].replaceAll("]$", templateToTest + "]");
        m_rm.getTemplate(templateToTest);
    }

    @Test
    public void listComputeTemplates() throws Exception  {
        List<String> templates = m_rm.listTemplates(Type.COMPUTE);
        assertTrue(templates.size()>0);
        System.out.println(templates.get(0));
        // Details of a template
        ResourceDescription rd = m_rm.getTemplate(templates.get(0));
        assertTrue(rd instanceof ComputeDescription);
        this.dumpDescription(rd);
    }
    

    @Test
    public void listStorageTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.STORAGE));
    }

    @Test
    public void listNetworkTemplates() throws NotImplementedException, TimeoutException, NoSuccessException  {
        assertNotNull(m_rm.listTemplates(Type.NETWORK));
    }

    ////////////
    // Resources
    ////////////
    @Test
    public void listComputeResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.COMPUTE));
    }

    @Test
    public void listStorageResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.STORAGE));
    }

    @Test
    public void listNetworkResources() throws NotImplementedException, TimeoutException, 
        NoSuccessException, AuthenticationFailedException, AuthorizationFailedException  {
        assertNotNull(m_rm.listResources(Type.NETWORK));
    }

    @Test
    public void list10Servers() throws Exception, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException, PermissionDeniedException, 
            IncorrectStateException {
        List<String> resources = m_rm.listResources(Type.COMPUTE);
        assertTrue(resources.size()>0);
        int count = 1;
        for (String serverId: resources) {
            Compute server = m_rm.acquireCompute(serverId);
            this.dumpCompute(server);
            if (count++ == 10) {
                return;
            }
        }
    }
    
    //////////
    // Acquire
    //////////
    @Test
    public void launchAndDeleteVM() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templateListForAcquire);
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server.getState());
        this.dumpCompute(server);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            if (!"openstack".equals(c.getAttribute(Context.TYPE))) {
                m_logger.info(c);
            }
        }
        m_rm.releaseCompute(server.getId());
    }
    
    @Test
    public void launchAndReconfigureDeleteVM() throws Exception {
        ComputeDescription cd;
        cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templateListForAcquire);
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        this.dumpCompute(server);
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templateListForReconfigure);
        server.reconfigure(cd);
        server.waitFor(240, State.ACTIVE);
        this.dumpCompute(server);
        m_rm.releaseCompute(server.getId());
    }

    @Test
    public void launchAndSubmitJobAndDeleteVM() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templateListForAcquire);
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server.getState());
        this.dumpCompute(server);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            if ("UserPass".equals(c.getAttribute(Context.TYPE))) {
                m_logger.info("UserPass context" + c);
            }
        }
        try {
            URL jobservice = URLFactory.createURL(server.getAccess()[0]);
            JobService service = JobFactory.createJobService(m_session, jobservice);
            JobDescription desc = JobFactory.createJobDescription();
            desc.setAttribute(JobDescription.EXECUTABLE, "/bin/date");
            desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
            desc.setAttribute(JobDescription.ERROR, "stderr.txt");
            Job job = service.createJob(desc);
            job.run();
            m_logger.info(job.getAttribute(Job.JOBID));   // for detecting hang in run()
    
            // wait for the END
            job.waitFor();
            m_logger.info("Job finished.");               // for detecting hang in waitFor()
    
            // check job status
            Assert.assertEquals(
                    org.ogf.saga.task.State.DONE,
                    job.getState());
        } catch (Exception e) {
            m_logger.error("Could not run job", e);
        }
        m_rm.releaseCompute(server.getId());
    }
    
    
    ////////
    // Utils
    ////////
    
    protected void dumpDescription(ResourceDescription rd) throws Exception {
        for (String a: rd.listAttributes()) {
            System.out.println("  * " + a + "=" + rd.getAttribute(a));
        }
    }
    
    protected void dumpCompute(Compute server) throws Exception {
        ResourceDescription rd = server.getDescription();
        assertNotNull(rd);
        System.out.println(server.getId());
        this.dumpDescription(rd);
        // display status
        System.out.println("  * status=" + server.getState().name() + " // " 
                + server.getMetric(ResourceTask.RESOURCE_STATEDETAIL).getAttribute(Metric.VALUE));
        for (String access: server.getAccess()) {
            System.out.println("  => " + access);
        }
    }
}
