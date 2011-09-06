package fr.in2p3.jsaga.adaptor.unicore.job;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import org.unigrids.x2006.x04.services.tss.SubmitDocument;
import org.unigrids.x2006.x04.services.tss.SubmitDocument.Submit;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.client.TSSClient;
import de.fzj.unicore.wsrflite.xmlbeans.BaseFault;

import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.advanced.HoldableJobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/

public class UnicoreJobControlAdaptor extends UnicoreJobAdaptorAbstract 
	implements JobControlAdaptor, HoldableJobAdaptor, StagingJobAdaptorTwoPhase {

    protected static final String PRE_STAGING_TRANSFERS_TAGNAME = "PreStagingIn";
    protected static final String POST_STAGING_TRANSFERS_TAGNAME = "PostStagingOut";

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

	public String getStagingDirectory(String nativeJobId)
			throws PermissionDeniedException, TimeoutException,
			NoSuccessException {
		// the staging directory is managed by Unicore
		return null;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		return getStagingTransfers(nativeJobId, PRE_STAGING_TRANSFERS_TAGNAME);
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId)
			throws PermissionDeniedException, TimeoutException, NoSuccessException {
		return getStagingTransfers(nativeJobId, POST_STAGING_TRANSFERS_TAGNAME);
	}

	public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		UnicoreJob j;
		try {
			j = new UnicoreJob(nativeJobId, m_uassecprop);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		j.start();
	}
	
    /**
     * Extract the staging URL from the JSDL as follows:
     * 
     * <jsaga:DataStaging>
     *   <jsaga:PreStagingIn>
     *   <jsaga:FileName>xxx</jsaga:FileName>
     *   <jsaga:Source>
     *     <jsaga:URI>/tmp/yyy</jsaga:URI>
     *   </jsaga:Source>
     *   <jsaga:Target>
     *     <jsaga:URI>unicore://<host>:<port>/xxx/</jsaga:URI>
     *   </jsaga:Target>
     * </jsaga:DataStaging>
     * 
     * or
     * <jsaga:DataStaging>
     *   <jsaga:PostStagingOut>
     *   <jsaga:FileName>xxx</jsaga:FileName>
     *   <jsaga:Source>
     *     <jsaga:URI>unicore://<host>:<port>/xxx/</jsaga:URI>
     *   </jsaga:Source>
     *   <jsaga:Target>
     *     <jsaga:URI>/tmp/yyy</jsaga:URI>
     *   </jsaga:Target>
     * </jsaga:DataStaging>
     * 
     * The Unicore data plugin URI is built like this: 
     * unicore://<host>:<port>/<FileName>?Target=<target>&Res=<JobId>
     * 
     * @param nativeJobId the native job identifier
     * @param jsdl the JSDL Job Description
     * @param preOrPost PRE_STAGING or POST_STAGING
     * @return the array of StagingTransfer defined in the JobDescription
     */
    protected StagingTransfer[] getStagingTransfers(String nativeJobId, String preOrPost) throws NoSuccessException{
    	StagingTransfer[] st = new StagingTransfer[]{};
    	JobDescriptionType jsdl;
		try {
			jsdl = new UnicoreJob(nativeJobId, m_uassecprop).getDescription();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
    	ArrayList transfers = new ArrayList();
    	// Extract <jobId> from nativeJobID URL (extract xxx from https://.../JobManagement?res=xxx)
    	String jobId;
		try {
			jobId = new URI(nativeJobId).getQuery().split("=")[1];
		} catch (URISyntaxException e) {
			throw new NoSuccessException(e);
		}
    	String from, to, host;
    	int port;
    	try {
			host = new URI(m_epr.getAddress().getStringValue()).getHost();
			port = new URI(m_epr.getAddress().getStringValue()).getPort();
		} catch (URISyntaxException e) {
			throw new NoSuccessException(e);
		}
    	for (XmlObject jsaga_ds : getElementsByTagName(jsdl, "DataStaging")) {
    		String remoteFile = "unicore://" + host + ":" + port + "/"
    			//+ getFirstNodeByTagName(jsaga_ds, "FileName").getTextContent()
    			+ getElementsByTagName(jsaga_ds, "FileName")[0].getDomNode().getFirstChild().getNodeValue()
    			//+ jsdl.selectChildren(new QName("http://www.in2p3.fr/jsdl-extension","FileName","jsaga"))[0].xmlText()
    			+ "?Target=" + m_target + "&Res=" +jobId;
    		//System.out.println(remoteFile.toString());
    		if (preOrPost.equals(PRE_STAGING_TRANSFERS_TAGNAME) && getElementsByTagName(jsaga_ds, PRE_STAGING_TRANSFERS_TAGNAME).length > 0) {
    			from = getElementsByTagName(getElementsByTagName(jsaga_ds, "Source")[0],"URI")[0].getDomNode().getFirstChild().getNodeValue();
    			transfers.add(new StagingTransfer(from, remoteFile, false));
    		} else if (preOrPost.equals(POST_STAGING_TRANSFERS_TAGNAME) && getElementsByTagName(jsaga_ds, POST_STAGING_TRANSFERS_TAGNAME).length > 0) {
    			to = getElementsByTagName(getElementsByTagName(jsaga_ds, "Target")[0],"URI")[0].getDomNode().getFirstChild().getNodeValue();
    			transfers.add(new StagingTransfer(remoteFile, to, false));
        		System.out.println(remoteFile + "->" + to);
    		}
    	}
    	return (StagingTransfer[]) transfers.toArray(st);
    }
	
    private static XmlObject[] getElementsByTagName(XmlObject obj, String elmt)  {
    	return obj.selectChildren(new QName("http://www.in2p3.fr/jsdl-extension", elmt, "jsaga"));
    }
    
    private static Node getFirstNodeByTagName(XmlObject xml, String tag) {
    	NodeList nl = xml.getDomNode().getChildNodes();
    	for (int i=0; i<nl.getLength(); i++) {
    		if (nl.item(i).getLocalName().equals(tag)) {
    			return nl.item(i);
    		}
    	}
    	return null;
    }
}
