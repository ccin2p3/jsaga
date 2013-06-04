package integration;

import fr.in2p3.jsaga.adaptor.cream.job.CreamStub;
import fr.in2p3.jsaga.adaptor.cream.job.DelegationStub;
import org.glite.x2007.x11.ce.cream.CREAMLocator;
import org.glite.x2007.x11.ce.cream.CREAMPort;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.rpc.ServiceException;

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
        System.setProperty("sslCAFiles", new File(new File(new File(System.getProperty("user.home"),".globus"),"certificates"),"*.0").getAbsolutePath());

        // set filter
        org.glite.x2007.x11.ce.cream.types.JobFilter filter = new org.glite.x2007.x11.ce.cream.types.JobFilter();
        if (m_delegationId != null) {
            filter.setDelegationId(m_delegationId);
        }

        // purge jobs
//        CreamStub creamStub = new CreamStub(m_url.getHost(), m_url.getPort(), DelegationStub.ANY_VO);
    	CREAMLocator cream_service = new CREAMLocator();
    	try {
			// TODO: check CREAM2 ou CREAM ???
			cream_service.setCREAM2EndpointAddress(m_url.getString());
			CREAMPort creamStub = cream_service.getCREAM2();
	        org.glite.x2007.x11.ce.cream.types.Result[] resultArray = creamStub.jobPurge(filter).getResult();
	        System.out.println(resultArray.length+" have been purged!");
		} catch (ServiceException e) {
            throw new BadParameterException(e.getMessage(), e);
		}
    }
}
