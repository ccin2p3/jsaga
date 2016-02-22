package org.ogf.saga.resource;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
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
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public abstract class ComputeTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

    // configuration
    protected String SUBMIT_DELAY           = "submitDelay";
    protected String ACQUIRE_TEMPLATE       = "acquireTemplate";
    protected String RECONFIGURE_TEMPLATE   = "reconfigureTemplate";

    protected List<String> m_templatesForAcquire;
    protected List<String> m_templatesForReconfigure;
    protected int m_delayBeforeSubmittingJobInSeconds;
    

    // defaults
    private static final String DEFAULT_SUBMIT_DELAY = "0";
    

    public ComputeTest(String resourceprotocol) throws Exception {
        super(resourceprotocol);
        m_delayBeforeSubmittingJobInSeconds = Integer.parseInt(super.getOptionalProperty(resourceprotocol, SUBMIT_DELAY, DEFAULT_SUBMIT_DELAY));
        m_templatesForAcquire = super.getProperties(resourceprotocol, ACQUIRE_TEMPLATE);
        m_templatesForReconfigure = super.getProperties(resourceprotocol, RECONFIGURE_TEMPLATE);
    }

    protected Object[] typeToBeTested() {
        return new Object[][] {
                {Type.COMPUTE}
        };
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
    
}
