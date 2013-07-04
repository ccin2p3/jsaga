package integration;

import eu.emi.security.canl.axis2.CANLAXIS2SocketFactory;
import fr.in2p3.jsaga.adaptor.cream.job.CreamStub;
import fr.in2p3.jsaga.adaptor.cream.job.DelegationStub;

import org.apache.commons.httpclient.protocol.Protocol;
import org.glite.ce.creamapi.ws.cream2.CREAMStub;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobFilter;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.JobPurgeRequest;
import org.glite.ce.creamapi.ws.cream2.CREAMStub.Result;
//import org.glite.ce.creamapi.ws.cream2.CREAMLocator;
//import org.glite.ce.creamapi.ws.cream2.CREAMPort;
//import org.glite.ce.creamapi.ws.cream2.types.JobFilter;
//import org.glite.ce.creamapi.ws.cream2.types.Result;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;

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
public class CreamExecutionPurgeJobs extends AbstractTest {
    private URL m_url;
    private String m_delegationId;

    public CreamExecutionPurgeJobs() throws Exception {
        this("cream");
    }
    protected CreamExecutionPurgeJobs(String jobprotocol) throws Exception {
        super();
        m_url = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL));
        String query = m_url.getQuery();
        if (query!=null && query.startsWith("delegationId=")) {
            m_delegationId = query.substring(query.indexOf('=')+1);
        } else {
            m_delegationId = null;
        }
    }

    public void test_purge() throws Exception {
        Protocol.registerProtocol("https", new Protocol("https", new CANLAXIS2SocketFactory(), m_url.getPort()));
        
        Properties m_sslConfig = new Properties();
        m_sslConfig.put("truststore", new File(
        									new File(
        										new File(
        											new File(System.getProperty("user.home"), ".jsaga"),
        										"contexts"),
        									"voms"),
        								"certificates")
        								.getAbsolutePath());
        m_sslConfig.put("proxy", new File(
        							new File(
    										new File(System.getProperty("user.home"), ".jsaga"),
        							"tmp"),
        						 "voms_cred.txt")
        						 .getAbsolutePath());
        							
        CANLAXIS2SocketFactory.setCurrentProperties(m_sslConfig);

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
