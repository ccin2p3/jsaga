package integration;

import org.glite.ce.creamapi.ws.cream2.CREAMStub;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobPurgeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Result;

//import javax.xml.rpc.ServiceException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionPurgeJobs
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   30 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionPurgeJobs extends CreamAbstractTest {

    public CreamExecutionPurgeJobs() throws Exception {
        super();
    }

    public void test_purge() throws Exception {
        // set filter
        JobFilter filter = new JobFilter();
        if (m_delegationId != null) {
            filter.setDelegationId(m_delegationId);
        }
        JobPurgeRequest jpr = new JobPurgeRequest();
        jpr.setJobPurgeRequest(filter);
        String url = new java.net.URL("https", m_url.getHost(), m_url.getPort(), "/ce-cream/services/CREAM2").toString();
        CREAMStub creamStub = new CREAMStub(url);
        try {
        	Result[] resultArray = creamStub.jobPurge(jpr).getJobPurgeResponse().getResult();
        	System.out.println(resultArray.length+" have been purged!");
        } catch (NullPointerException npe) {
        	System.out.println("no jobs have been purged!");
        }
    }
}
