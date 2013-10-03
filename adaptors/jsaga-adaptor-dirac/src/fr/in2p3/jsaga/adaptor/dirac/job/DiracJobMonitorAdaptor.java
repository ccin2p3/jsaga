package fr.in2p3.jsaga.adaptor.dirac.job;

import java.net.URL;

import org.json.simple.JSONObject;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracRESTClient;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DiracJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   30 sept 2013
* ***************************************************/
public class DiracJobMonitorAdaptor extends DiracJobAdaptorAbstract implements
		QueryIndividualJob/*, QueryListJob, ListableJobAdaptor, JobInfoAdaptor*/ {

	public JobStatus getStatus(String nativeJobId) throws TimeoutException, NoSuccessException {
		try {
			JSONObject jobInfo = new DiracRESTClient(m_credential, m_accessToken)
					.get(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId));
			return new DiracJobStatus(nativeJobId, jobInfo);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
}
