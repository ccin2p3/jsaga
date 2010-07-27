package integration;

import org.ogf.saga.error.SagaException;
import org.ogf.saga.job.*;
import org.ogf.saga.job.abstracts.AbstractJobTest;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WMSExecutionTestSuiteJDL
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   13 oct. 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WMSExecutionTestSuiteJDL extends AbstractJobTest {
    public WMSExecutionTestSuiteJDL() throws Exception {
        super("wms");
    }

    public void test_jdl() throws SagaException {
        JobDescription desc = JobFactory.createJobDescription();
        desc.setAttribute(JobDescription.EXECUTABLE, "myScript.sh");
        desc.setAttribute(JobDescription.INPUT, "stdin.txt");
        desc.setAttribute(JobDescription.OUTPUT, "stdout.txt");
        desc.setVectorAttribute(JobDescription.FILETRANSFER, new String[]{"myScript.sh > myScript.sh"});
        JobService service = JobFactory.createJobService(m_session, m_jobservice);
        Job job = service.createJob(desc);
        System.out.println(job.getAttribute("NativeJobDescription"));
    }
}
