 package fr.in2p3.jsaga.adaptor.arex.job;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobControlAdaptorAbstract;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorXSLT;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingJobAdaptorTwoPhase;
import fr.in2p3.jsaga.adaptor.job.control.staging.StagingTransfer;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;

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

import org.apache.axis.client.Stub;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.message.Text;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStaging_Type;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.Resources_Type;
import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;
import org.nordugrid.schemas.arex.ActivitySubStateType;
import org.nordugrid.schemas.besFactory.ActivityStateEnumeration;
import org.nordugrid.schemas.besFactory.ActivityStatusType;
import org.nordugrid.schemas.besFactory.CantApplyOperationToCurrentStateFaultType;
import org.nordugrid.schemas.besFactory.InvalidActivityIdentifierFaultType;
import org.nordugrid.schemas.besFactory.NotAuthorizedFaultType;
import org.nordugrid.schemas.besFactory.OperationWillBeAppliedEventuallyFaultType;
import org.nordugrid.schemas.besFactory.holders.ActivityStatusTypeHolder;

import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryExpressionType;
import org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType;
import org.ogf.saga.error.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
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


	public static final String AREX_NAMESPACE_URI = "http://www.nordugrid.org/schemas/a-rex";
	
	protected ARex_PortType _arex_pt = null;
	//protected QueryResourcePropertiesResponse _arex_resources = null;
	
	// TODO : use array of hashtables
	protected Hashtable _arex_resources = new Hashtable();
	
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
		
        try {
            SOAPHeaderElement she = new SOAPHeaderElement(ArexJobMonitorAdaptor.WSA_NS,
            		"Action",
            		"http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest");
            		
            ((Stub)_arex_pt).clearHeaders();
            ((Stub)_arex_pt).setHeader(she);

            String computingShareRoot = "//glue:Services/glue:ComputingService/glue:ComputingShares/glue:ComputingShare/";
            // solution 1: some specific nodes
            //String xpathQuery = computingShareRoot + "glue:MaxVirtualMemory";
        	//xpathQuery += " | " + computingShareRoot + "glue:MaxWallTime";
        	
        	// solution 2: all nodes except Associations
        	//String xpathQuery = "//glue:Services/glue:ComputingService/glue:ComputingShares/glue:ComputingShare/*[not(self::glue:Associations)]";
        	
            // Solutions 1 and 2 do not work. XPATH is Ok but any[] has 1 element only
        	/*QueryExpressionType query = new QueryExpressionType();
            query.setDialect(new org.apache.axis.types.URI(javax.xml.crypto.dsig.Transform.XPATH));
            MessageElement me = new MessageElement(new Text(xpathQuery));
            query.set_any(new MessageElement[]{me});

			_arex_resources = _arex_pt.queryResourceProperties(query);
						
			for (MessageElement computingParam: _arex_resources.get_any()) {
				System.out.println(computingParam.getAsString());
			}
			*/
        	String computingParams[] = new String[]{"MaxVirtualMemory","MaxWallTime"};
        	for (String p: computingParams) {
                ((Stub)_arex_pt).clearHeaders();
                ((Stub)_arex_pt).setHeader(she);
                String xpathQuery = computingShareRoot + "glue:" + p;
            	QueryExpressionType query = new QueryExpressionType();
                query.setDialect(new org.apache.axis.types.URI(javax.xml.crypto.dsig.Transform.XPATH));
                MessageElement me = new MessageElement(new Text(xpathQuery));
                query.set_any(new MessageElement[]{me});

                QueryResourcePropertiesResponse resp = _arex_pt.queryResourceProperties(query);
        		if (resp != null) {
        			_arex_resources.put(p, resp.get_any()[0].getAsString());
        		}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//throw new NoSuccessException("TEST");
        
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
	
	
    /**
     * Check required resources against available resources
     * 
     * @param required_resources
     * @throws BadResource if available resources do not match with required resources
     */
    protected void checkResources(Resources_Type required_resources) throws BadResource {
		if (required_resources == null) return;
		if (_arex_resources.size() == 0) return;
		// checkMatch, check resources asked (jsdl_type.getJobDescription().getResources()) VS resources available in A-REX
		if (required_resources.getTotalVirtualMemory() != null &&
				required_resources.getTotalVirtualMemory().getUpperBoundedRange() != null &&
				_arex_resources.containsKey("MaxVirtualMemory") &&
				required_resources.getTotalVirtualMemory().getUpperBoundedRange().get_value() > (Integer)_arex_resources.get("MaxVirtualMemory")) {
			throw new BadResource("Too much virtual memory required");
		}
		// Call generic BES check
		super.checkResources(required_resources);
    }
    
	
	
	
	private void changeStatus(String nativeJobId, ActivityStateEnumeration oldStatus, ActivityStateEnumeration newStatus, ActivitySubStateType newSubState) throws PermissionDeniedException, NoSuccessException {
		//ActivityStatusType oldAst = new ActivityStatusType();
		//oldAst.setState(oldStatus);
		ActivityStatusType newAst = new ActivityStatusType();
		newAst.setState(newStatus);
		//MessageElement substate = new MessageElement();
		//substate.setName("state");
		//substate.setNamespaceURI(AREX_NAMESPACE_URI);
		//substate.addTextNode(newSubState.toString());
		//MessageElement[] any = new MessageElement[]{substate};
		//newAst.setAny(any);
		//request.setNewStatus(newAst);
		try {
			_arex_pt.changeActivityStatus(nativeId2ActivityId(nativeJobId),	null, new ActivityStatusTypeHolder(newAst)) ;
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
	}
}