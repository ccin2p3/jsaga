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

    // configuration
    protected String SUBMIT_DELAY           = "submitDelay";
    protected String ACQUIRE_TEMPLATE       = "acquireTemplate";
    protected String RECONFIGURE_TEMPLATE   = "reconfigureTemplate";

    // defaults
    private static final String DEFAULT_SUBMIT_DELAY = "0";
    
    protected URL m_resourcemanager;
    protected Session m_session;
    protected ResourceManager m_rm;

    protected List<String> m_templatesForAcquire;
    protected List<String> m_templatesForReconfigure;
    protected int m_delayBeforeSubmittingJobInSeconds;
    
    protected ResourceBaseTest(String resourceprotocol) throws Exception {
        super();

        // configure
        m_resourcemanager = URLFactory.createURL(getRequiredProperty(resourceprotocol, CONFIG_RM_URL));
        m_session = SessionFactory.createSession(true);
        m_delayBeforeSubmittingJobInSeconds = Integer.parseInt(super.getOptionalProperty(resourceprotocol, SUBMIT_DELAY, DEFAULT_SUBMIT_DELAY));
        m_templatesForAcquire = super.getProperties(resourceprotocol, ACQUIRE_TEMPLATE);
        m_templatesForReconfigure = super.getProperties(resourceprotocol, RECONFIGURE_TEMPLATE);
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
    
    ////////////
    // Templates
    ////////////
    @Test
    public void getTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        m_rm.getTemplate(m_templatesForAcquire.get(0));
    }

    @Test(expected = DoesNotExistException.class)
    public void unknownTemplate() throws NotImplementedException, BadParameterException, 
            IncorrectURLException, AuthenticationFailedException, AuthorizationFailedException, 
            TimeoutException, NoSuccessException, DoesNotExistException {
        String templateToTest = "thisTemplateDoesNotExists";
        // Take the first template and insert "thisTemplateDoesNotExists" just before the last ']'
        templateToTest = m_templatesForAcquire.get(0).replaceAll("]$", templateToTest + "]");
        m_rm.getTemplate(templateToTest);
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
    //////////
    // Acquire
    //////////
    @Test
    public void launchAndDeleteVM() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[m_templatesForAcquire.size()];
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templatesForAcquire.toArray(templates));
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server.getState());
        this.dumpResource(server);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            m_logger.info("** Context :" + c.getAttribute(Context.TYPE));
            m_logger.debug(c.toString());
        }
        m_rm.releaseCompute(server.getId());
    }
    
    @Test
    public void launchAndDelete2VMs() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[m_templatesForAcquire.size()];
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templatesForAcquire.toArray(templates));
        Compute server1 = m_rm.acquireCompute(cd);
        server1.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server1.getState());
        this.dumpResource(server1);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            m_logger.info("** Context :" + c.getAttribute(Context.TYPE));
            m_logger.debug(c.toString());
        }
        Compute server2 = m_rm.acquireCompute(cd);
        server2.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server2.getState());
        this.dumpResource(server2);
        // release both VMs
        m_rm.releaseCompute(server1.getId());
        m_rm.releaseCompute(server2.getId());
    }
    
    @Test
    public void launchAndReconfigureDeleteVM() throws Exception {
        ComputeDescription cd;
        cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[m_templatesForAcquire.size()];
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templatesForAcquire.toArray(templates));
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        this.dumpResource(server);
        templates = new String[m_templatesForReconfigure.size()];
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templatesForReconfigure.toArray(templates));
        server.reconfigure(cd);
        server.waitFor(240, State.ACTIVE);
        this.dumpResource(server);
        m_rm.releaseCompute(server.getId());
    }

    @Test
    public void launchAndSubmitJobAndDeleteVM() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[m_templatesForAcquire.size()];
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, m_templatesForAcquire.toArray(templates));
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server.getState());
        this.dumpResource(server);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            m_logger.info("** Context :" + c.getAttribute(Context.TYPE));
            m_logger.debug(c.toString());
        }
        if (m_delayBeforeSubmittingJobInSeconds > 0) {
            Thread.sleep(m_delayBeforeSubmittingJobInSeconds*1000);
        }
        org.ogf.saga.task.State jobState = null;
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
            jobState = job.getState();
        } catch (Exception e) {
            m_logger.error("Could not run job", e);
            fail(e.getMessage());
        } finally {
            m_rm.releaseCompute(server.getId());
        }
        // check job status
        Assert.assertEquals(
                org.ogf.saga.task.State.DONE,
                jobState);
    }
    
    //////////////////
    // acquire storage
    //////////////////
    @Test
    public void createAndMkdirAndDeleteStorageArea() throws Exception {
        StorageDescription sd = (StorageDescription) ResourceFactory.createResourceDescription(Type.STORAGE);
        Storage storage = m_rm.acquireStorage(sd);
        storage.waitFor(120, State.ACTIVE);
        this.dumpResource(storage);
        URL baseUrl = URLFactory.createURL(storage.getAccess()[0]);
        URL m_dirUrl = createURL(baseUrl, "dir/");
        NSDirectory m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.CREATE.or(Flags.EXCL));
        m_dir.close();
        storage.release();
    }
    
    //////////////////
    // Network
    //////////////////
    @Test
    public void createAndDeleteNetwork() throws Exception {
        NetworkDescription nd = (NetworkDescription) ResourceFactory.createResourceDescription(Type.NETWORK);
        Network net = m_rm.acquireNetwork(nd);
        net.waitFor(120, State.ACTIVE);
        this.dumpResource(net);
        net.release();
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
