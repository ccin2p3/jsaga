package integration;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
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
import org.ogf.saga.resource.ResourceBaseTest;
import org.ogf.saga.resource.ResourceFactory;
import org.ogf.saga.resource.Type;
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;
import org.ogf.saga.resource.instance.Compute;
import org.ogf.saga.resource.manager.ResourceManager;
import org.ogf.saga.resource.task.State;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;


public class OpenstackResourceAdaptorTest extends ResourceBaseTest {

    private Logger m_logger = Logger.getLogger(this.getClass());

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
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[]{
                "[DUMMY_URL]-[nova/images/official-centosCC-7x-x86_64]",
                "[DUMMY_URL]-[nova/flavors/m1.small]"
        };
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, templates);
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        assertEquals(State.ACTIVE, server.getState());
        this.dumpCompute(server);
        // test if we have a new UserPass context in the session
        for (Context c: m_session.listContexts()) {
            if ("UserPass".equals(c.getAttribute(Context.TYPE))) {
                m_logger.info(c);
            }
        }
        m_rm.releaseCompute(server.getId());
    }
    
    @Test
    public void launchAndReconfigureDeleteVM() throws Exception {
        ComputeDescription cd;
        String[] templates;
        cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        templates = new String[]{
                "[DUMMY_URL]-[nova/images/official-centosCC-7x-x86_64]",
                "[DUMMY_URL]-[nova/flavors/m1.small]"
        };
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, templates);
        Compute server = m_rm.acquireCompute(cd);
        server.waitFor(120, State.ACTIVE);
        this.dumpCompute(server);
        // reconfigure to official-ubuntu-14.04-x86_64 
        templates = new String[]{
                "[DUMMY_URL]-[nova/images/official-ubuntu-14.04-x86_64]",
                "[DUMMY_URL]-[nova/flavors/m1.small]"
        };
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, templates);
        server.reconfigure(cd);
        server.waitFor(120);
        this.dumpCompute(server);
        m_rm.releaseCompute(server.getId());
    }

    @Test
    public void launchAndSubmitJobAndDeleteVM() throws Exception {
        ComputeDescription cd = (ComputeDescription) ResourceFactory.createResourceDescription(Type.COMPUTE);
        String[] templates = new String[]{
                "[DUMMY_URL]-[nova/images/official-centosCC-7x-x86_64]",
                "[DUMMY_URL]-[nova/flavors/m1.small]"
        };
        cd.setVectorAttribute(ResourceDescription.TEMPLATE, templates);
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
        // Wait 10s for the SSHD server to be started
//        Thread.sleep(30000);
        try {
            URL jobservice = URLFactory.createURL(server.getAccess()[0]);
//            URL jobservice = URLFactory.createURL("ssh://172.17.0.37");
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
//        m_rm.releaseCompute(server.getId());
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
