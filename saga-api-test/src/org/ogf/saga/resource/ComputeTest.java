package org.ogf.saga.resource;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
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
import org.ogf.saga.resource.instance.Resource;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

public abstract class ComputeTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

    public ComputeTest(String resourceprotocol) throws Exception {
        super(resourceprotocol, Type.COMPUTE);
    }

    @Override
    protected Resource acquire(ResourceDescription rd) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        return m_rm.acquireCompute((ComputeDescription) rd);
    }
    
    @Test 
    public void acquireWithFlavorRequirements() throws Exception {
        int ram = 1024;
        int cpu = 2;
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        // image only
        cd.setVectorAttribute(ComputeDescription.TEMPLATE, 
                new String[]{m_templatesForAcquire.get(0)}
        );
        // add memory and size
        cd.setAttribute(ComputeDescription.MEMORY, Integer.toString(ram));
        cd.setAttribute(ComputeDescription.SIZE, Integer.toString(cpu));
        m_currentResource = this.acquireResourceFromDescReadyForUse(cd);
        ComputeDescription desc = ((Compute)m_currentResource).getDescription();
        assertTrue(desc.existsAttribute(ComputeDescription.MEMORY));
        assertTrue(Integer.parseInt(desc.getAttribute(ComputeDescription.MEMORY)) >= ram);
        assertTrue(desc.existsAttribute(ComputeDescription.SIZE));
        assertTrue(Integer.parseInt(desc.getAttribute(ComputeDescription.SIZE)) >= cpu);
    }
    
    //////////
    // run job
    //////////
    @Test
    public void launchAndSubmitJobAndDeleteVM() throws Exception {
        m_currentResource = this.acquireResourceReadyForUse();
        org.ogf.saga.task.State jobState = null;
        URL jobservice = URLFactory.createURL(m_currentResource.getAccess()[0]);
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
        // check job status
        Assert.assertEquals(
                org.ogf.saga.task.State.DONE,
                jobState);
    }
    
}
