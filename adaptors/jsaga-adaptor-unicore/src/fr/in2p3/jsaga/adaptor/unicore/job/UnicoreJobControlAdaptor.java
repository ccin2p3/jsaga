package fr.in2p3.jsaga.adaptor.unicore.job;

import java.util.Iterator;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import org.unigrids.x2006.x04.services.tss.SubmitDocument;
import org.unigrids.x2006.x04.services.tss.SubmitDocument.Submit;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/

public class UnicoreJobControlAdaptor extends UnicoreJobAdaptorAbstract implements JobControlAdaptor, HoldableJobAdaptor {

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	return new JobDescriptionTranslatorXSLT("xsl/job/unicore-jsdl.xsl");
    }

	public JobMonitorAdaptor getDefaultJobMonitor() {
		return new UnicoreJobMonitorAdaptor();
	}

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
    	
    	try {
		    // Find a target system
			TSFClient cl = new TSFClient(m_epr.getAddress().getStringValue(), m_epr, m_uassecprop);
			Iterator<EndpointReferenceType> flavoursIter = cl.getTargetSystems().iterator(); 
			if (flavoursIter.hasNext()) {
				EndpointReferenceType _tss_epr = flavoursIter.next();
		        m_client = new TSSClient(_tss_epr.getAddress().getStringValue(), _tss_epr, m_uassecprop);
		    } else {
		    	m_client = cl.createTSS();
		    }
			//throw new NoSuccessException("END");

		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    }

	public String submit(String jobDesc, boolean checkMatch, String uniqId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException, BadResource {
		try {
			SubmitDocument sd = SubmitDocument.Factory.newInstance();
			Submit sub = sd.addNewSubmit();
			JobDefinitionType jdt = JobDefinitionDocument.Factory.parse(jobDesc).getJobDefinition();
			sub.setJobDefinition(jdt);
			sd.setSubmit(sub);
			JobClient jc = m_client.submit(sd);
			try {
				// TODO parameter ?
				jc.waitUntilReady(600000); // 10mn
			} catch (Exception e) {
				throw new TimeoutException(e);
			}
			jc.start();
			return jc.getUrl();
		} catch (XmlException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public void cancel(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		try {
			new UnicoreJob(nativeJobId, m_uassecprop).cancel();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

	public boolean hold(String nativeJobId)	throws PermissionDeniedException, TimeoutException,	NoSuccessException {
		try {
			new UnicoreJob(nativeJobId, m_uassecprop).hold();
			return true;
		} catch (Exception e) {
			if (e instanceof BaseFault) {
				BaseFault fault = (BaseFault)e;
				if (fault.getMessage().startsWith("Could not hold")) {
					return false;
				}
			}
			throw new NoSuccessException(e);
		}
	}

	public boolean release(String nativeJobId) throws PermissionDeniedException,	TimeoutException, NoSuccessException {
		try {
			new UnicoreJob(nativeJobId, m_uassecprop).release();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
