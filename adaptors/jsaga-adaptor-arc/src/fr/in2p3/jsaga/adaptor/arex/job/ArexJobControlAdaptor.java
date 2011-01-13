 package fr.in2p3.jsaga.adaptor.arex.job;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.security.X509SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

/* for changeStatus
import org.apache.axis.message.MessageElement;
import org.nordugrid.schemas.besFactory.ActivityStateEnumeration;
import org.nordugrid.schemas.besFactory.ActivityStatusType;
import org.nordugrid.schemas.besFactory.holders.ActivityStatusTypeHolder;
import org.nordugrid.schemas.besFactory.CantApplyOperationToCurrentStateFaultType;
import org.nordugrid.schemas.besFactory.InvalidActivityIdentifierFaultType;
import org.nordugrid.schemas.besFactory.OperationWillBeAppliedEventuallyFaultType;
import org.nordugrid.schemas.besFactory.NotAuthorizedFaultType;

import org.nordugrid.schemas.arex.ChangeActivityStatusRequestType;
import org.nordugrid.schemas.arex.ChangeActivityStatusResponseType;
import org.nordugrid.schemas.arex.ActivitySubStateType;
*/

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStaging_Type;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;

import org.ogf.saga.error.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.xml.rpc.ServiceException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ArexJobControlAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/
public class ArexJobControlAdaptor extends BesJobControlAdaptorAbstract implements StagingJobAdaptorTwoPhase {

    //private static final String XSLTPARAM_PROTOCOL = "Protocol";
    //private static final String XSLTPARAM_HOST = "HostName";
    //private static final String XSLTPARAM_PORT = "Port";
    //private static final String XSLTPARAM_PATH = "Path";

    public static final String AREX_NAMESPACE_URI = "http://www.nordugrid.org/schemas/a-rex";
	
	protected ARex_PortType _arex_pt = null;
	
    public String getType() {
        return "arex";
    }
    
	public int getDefaultPort() {
		return 2010;
	}

	public Class getJobClass() {
		return ArexJob.class;
	}

    public JobMonitorAdaptor getDefaultJobMonitor() {
        return new ArexJobMonitorAdaptor();
    }

    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
    	return new JobDescriptionTranslatorXSLT("xsl/job/arex-jsdl.xsl");
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	super.connect(userInfo, host, port, basePath, attributes);
		
    	if (_arex_pt != null) return;
    	
        ARex_ServiceLocator _arex_service = new ARex_ServiceLocator();
		try {
			_arex_service.setEndpointAddress("ARex", _bes_url.toString());
			_arex_pt=(ARex_PortType) _arex_service.getARex();
		} catch (ServiceException e) {
			throw new NoSuccessException(e);
		}
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        super.disconnect();
    }

	/////////////////////////////////////
	// Implementation of StagingJobAdaptorTwoPhase
	/////////////////////////////////////
	public String getStagingDirectory(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// The staging directory is the A-REX SD "Session Directory" managed by A-REX
		return null;
	}

	public StagingTransfer[] getInputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException,NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
		// Extract PreStaging transfers
		return getStagingTransfers(nativeJobId, jsdl_type, PRE_STAGING_TRANSFERS_TAGNAME);
	}

	public StagingTransfer[] getOutputStagingTransfer(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// Get JobDefinition from BES service
    	JobDefinition_Type jsdl_type = getJobDescriptionTypeFromNativeId(nativeJobId);
		// Extract PostStaging transfers
		return getStagingTransfers(nativeJobId, jsdl_type, POST_STAGING_TRANSFERS_TAGNAME);
	}

	public void start(String nativeJobId) throws PermissionDeniedException, TimeoutException, NoSuccessException {
		// nothing to do
	}

	
    /**
     * Extract the staging URL from the JSDL as follows:
     * 
     * <jsdl:DataStaging>
     *   <jsdl:FileName>xxx<jsdl:FileName>
     *   <jsdl:Source/>
     *   <jsaga:Source><jsaga:URI>file:/tmp/xxx</jsaga:URI></jsaga:Source>	
     * </jsdl:DataStaging>
     * 
     * or
     * <jsdl:DataStaging>
     *   <jsdl:FileName>xxx<jsdl:FileName>
     *   <jsdl:Target/>
     *   <jsaga:Target><jsaga:URI>file:/tmp/xxx</jsaga:URI></jsaga:Source>	
     * </jsdl:DataStaging>
     * 
     * <jsdl:Source/> means A-REX will wait for the file to be uploaded in the SD
     * The target will be SD/FileName e;g. https://xxx:2010/arex-x509/XXXXX
     * 
     * For PostTransfers, the source URL will be SD/FileName e;g. https://xxx:2010/arex-x509/XXXXX
     * 
     * @param nativeJobId the native job identifier
     * @param jsdl_type the JSDL Job Definition
     * @param preOrPost PRE_STAGING or POST_STAGING
     * @return the array of StagingTransfer defined in the JobDescription
     */
    protected StagingTransfer[] getStagingTransfers(String nativeJobId, JobDefinition_Type jsdl_type, String preOrPost) throws NoSuccessException{
    	StagingTransfer[] st = new StagingTransfer[]{};
    	ArrayList transfers = new ArrayList();
    	String from, to;
    	for (DataStaging_Type dst: jsdl_type.getJobDescription().getDataStaging()){
    		if (preOrPost.equals(PRE_STAGING_TRANSFERS_TAGNAME) && dst.getSource() != null ) {
    			from = (dst.get_any())[0].getFirstChild().getFirstChild().getNodeValue();
    			// change scheme https to arex for the engine
    			URI httpsto, arexto;
				try {
					httpsto = new URI(nativeJobId + '/' + dst.getFileName());
					arexto = new URI(getType(),
											httpsto.getUserInfo(),
											httpsto.getHost(),
											httpsto.getPort(),
											httpsto.getPath(),
											httpsto.getQuery(),
											httpsto.getFragment());
				} catch (URISyntaxException e) {
					throw new NoSuccessException(e);
				}
				transfers.add(new StagingTransfer(from, arexto.toString(), false));
    		} else if (preOrPost.equals(POST_STAGING_TRANSFERS_TAGNAME) && dst.getTarget() != null) {
    			from = nativeJobId + '/' + dst.getFileName();
    			// change scheme https to arex for the engine
    			URI httpsfrom, arexfrom;
    			try {
					httpsfrom = new URI(from);
					arexfrom = new URI(getType(),
										httpsfrom.getUserInfo(),
										httpsfrom.getHost(),
										httpsfrom.getPort(),
										httpsfrom.getPath(),
										httpsfrom.getQuery(),
										httpsfrom.getFragment());
				} catch (URISyntaxException e) {
					throw new NoSuccessException(e);
				}
    			to = (dst.get_any())[0].getFirstChild().getFirstChild().getNodeValue();
				transfers.add(new StagingTransfer(arexfrom.toString(), to, false));
    		}
    	}
    	return (StagingTransfer[]) transfers.toArray(st);
    }
	
	
	
	
	
	/*private void changeStatus(String nativeJobId, ActivityStateEnumeration oldStatus, ActivityStateEnumeration newStatus, ActivitySubStateType newSubState) throws PermissionDeniedException, NoSuccessException {
		//ChangeActivityStatusRequestType request = new ChangeActivityStatusRequestType();
		//request.setActivityIdentifier(nativeId2ActivityId(nativeJobId));
		ActivityStatusType oldAst = new ActivityStatusType();
		oldAst.setState(oldStatus);
		ActivityStatusType newAst = new ActivityStatusType();
		newAst.setState(newStatus);
		MessageElement substate = new MessageElement();
		substate.setName("state");
		substate.setNamespaceURI(AREX_NAMESPACE_URI);
		substate.addTextNode(newSubState.toString());
		MessageElement[] any = new MessageElement[]{substate};
		newAst.setAny(any);
		//request.setNewStatus(newAst);
		try {
			_arex_pt.changeActivityStatus(nativeId2ActivityId(nativeJobId),	oldAst, new ActivityStatusTypeHolder(newAst)) ;
		} catch (OperationWillBeAppliedEventuallyFaultType e) {
			throw new NoSuccessException(e);
		} catch (CantApplyOperationToCurrentStateFaultType e) {
			throw new NoSuccessException(e);
		} catch (InvalidActivityIdentifierFaultType e) {
			throw new NoSuccessException(e);
		} catch (NotAuthorizedFaultType e) {
			throw new PermissionDeniedException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}*/
}