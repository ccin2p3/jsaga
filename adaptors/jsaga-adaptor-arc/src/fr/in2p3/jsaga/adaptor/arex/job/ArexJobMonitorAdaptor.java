package fr.in2p3.jsaga.adaptor.arex.job;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;

import org.apache.axis.client.Stub;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.message.Text;
import org.apache.axis.types.URI;
import org.apache.log4j.Logger;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStateEnumeration;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetActivityStatusesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;

import org.oasis_open.docs.wsrf.rp_2.QueryExpressionType;
import org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesArexJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   9 déc 2010
* ***************************************************/

public class ArexJobMonitorAdaptor extends BesJobMonitorAdaptor implements JobInfoAdaptor {
        
	protected ARex_PortType _arex_pt = null;
	private static final Integer NB_TRIES = 10;
	private static final String AREX_CREATIONTIME = "CreationTime";
	private static final String AREX_EXITCODE = "ExitCode";
	private static final String AREX_SUBMISSIONTIME = "SubmissionTime";
	private static final String AREX_ENDTIME = "EndTime";
	private static final String AREX_EXECUTIONNODE = "ExecutionNode";
	private static final String TIME_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssz";
	
	protected static final String WSA_NS = fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.AttributedQNameType.getTypeDesc().getXmlType().getNamespaceURI();
	
	public String getType() {
        return "arex";
    }

	public int getDefaultPort() {
		return 2010;
	}

	protected JobStatus getJobStatus(String nativeJobId, ActivityStatusType ast) throws NoSuccessException {
		try {
			if (ast.getState().equals(ActivityStateEnumeration.Finished) || ast.getState().equals(ActivityStateEnumeration.Failed)) {
				return new ArexJobStatus(nativeJobId, ast, getExitCode(nativeJobId, 1));
			} else {
				throw new NotImplementedException();
			}
		} catch (NotImplementedException nie) {
			return new ArexJobStatus(nativeJobId, ast);
		}
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
		//System.out.println(getInfo("https://interop.grid.niif.hu:2010/arex-x509;93413170414131940927376;JobID;http://www.nordugrid.org/schemas/a-rex","ExitCode",1));
		//throw new NoSuccessException("DUMMY");
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        super.disconnect();
    }

	public Integer getExitCode(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return getExitCode(nativeJobId, NB_TRIES);
	}

	private Integer getExitCode(String nativeJobId, Integer nbTries) throws NotImplementedException, NoSuccessException {
		return Integer.parseInt(getInfo(nativeJobId, AREX_EXITCODE, nbTries));
	}
	
	public Date getCreated(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		//throw new NotImplementedException();
		return getTime(nativeJobId, AREX_CREATIONTIME);
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		return getTime(nativeJobId, AREX_SUBMISSIONTIME);
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		return getTime(nativeJobId, AREX_ENDTIME);
	}

	private Date getTime(String nativeJobId, String whichTime) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat(TIME_ISO8601);
		try {
			return df.parse(getInfo(nativeJobId, whichTime, NB_TRIES).replaceAll("Z","UTC"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}		
	}
	
	public String[] getExecutionHosts(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return new String[]{
				getInfo(nativeJobId, AREX_EXECUTIONNODE, NB_TRIES)
		};
	}

	// Wrapper for getInfoXML or getInfoWSRF or getInfoBES
	private String getInfo(String nativeJobId, String infoName, Integer nbTries) throws NotImplementedException, NoSuccessException {
		return getInfoBES( nativeJobId,  infoName, nbTries);
	}
	
	private String getInfoBES(String nativeJobId, String infoName, Integer nbTries) throws NotImplementedException, NoSuccessException {
		try {
    		int loop = 0;
    		while (loop < nbTries) {
				GetActivityStatusesType requestStatus = new GetActivityStatusesType();
				EndpointReferenceType[] refs = new EndpointReferenceType[1];
				refs[0] = nativeId2ActivityId(nativeJobId);
				requestStatus.setActivityIdentifier(refs);
				// add AREX extension to get more attributes
				MessageElement arexExtensions = new MessageElement("ActivityStatusVerbosity", "a-rex", ArexJobControlAdaptor.AREX_NAMESPACE_URI);
				arexExtensions.addTextNode("Full");
				requestStatus.set_any(new MessageElement[]{arexExtensions});
				Logger.getLogger(ArexJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(requestStatus));
				GetActivityStatusesResponseType responseStatus = _bes_pt.getActivityStatuses(requestStatus);
				Logger.getLogger(ArexJobMonitorAdaptor.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(responseStatus));
				// Search for <ComputingActivity
				for (MessageElement me : responseStatus.getResponse(0).getActivityStatus().get_any()) {
					if (me.getName().equals("ComputingActivity")) {
						// CreationTime is defined as an attribute of ComputingActivity node
						if (AREX_CREATIONTIME.equals(infoName))
							return me.getAttribute(AREX_CREATIONTIME);
						// all other info are children nodes
						java.util.Iterator iter = me.getChildElements();
						while (iter.hasNext()) {
							MessageElement jobinfo = (MessageElement)(iter.next());
							if (jobinfo.getName().equals(infoName)) {
								return jobinfo.getFirstChild().getNodeValue();
							}
						}
					}
				}
				loop++;
				// Do not sleep at last attempt
	    		if (loop < nbTries) Thread.sleep(5000);
    		}
			throw new NotImplementedException("Could not get " + infoName);
		} catch (InvalidRequestMessageFaultType e) {
			throw new NoSuccessException(e);
		} catch (RemoteException e) {
			throw new NoSuccessException(e);
		} catch (SOAPException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		}
		
	}
	/**
	 * get job information through the WSRP method
	 * @param nativeJobId : the job id
	 * @param infoName : the name of the information to get
	 * @param nbTries : the number of tries before considering the query does not work
	 * @return the job information
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 * @deprecated
	 */
	private String getInfoWSRP(String nativeJobId, String infoName, Integer nbTries) throws NotImplementedException, NoSuccessException {
        try {
    		int loop = 0;
    		while (loop < nbTries) {
	            SOAPHeaderElement she = new SOAPHeaderElement(WSA_NS,
	            		"Action",
	            		"http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest");
	            		
	            ((Stub)_arex_pt).clearHeaders();
	            ((Stub)_arex_pt).setHeader(she);
	        	
	        	String xpathQuery = "//glue:Services/glue:ComputingService/glue:ComputingEndpoint/glue:ComputingActivities/glue:ComputingActivity/glue:IDFromEndpoint[.='" + nativeJobId + "']/../glue:" + infoName;
	
	        	QueryExpressionType query = new QueryExpressionType();
	            query.setDialect(new URI(javax.xml.crypto.dsig.Transform.XPATH));
	            MessageElement me = new MessageElement(new Text(xpathQuery));
	            query.set_any(new MessageElement[]{me});
	
	            QueryResourcePropertiesResponse response = _arex_pt.queryResourceProperties(query);
	            
	            if (response != null) {
	            	/* for direct access to infoName */
	            	return response.get_any()[0].getAsString();
	            }
				loop++;
				// Do not sleep at last attempt
	    		if (loop < nbTries) Thread.sleep(5000);
    		}
			throw new NotImplementedException("Could not get " + infoName);
        } catch (ResourceUnknownFaultType e) {
        	throw new NotImplementedException(e);
        } catch (ResourceUnavailableFaultType e) {
        	throw new NotImplementedException(e);
        } catch (NotImplementedException nie) {
        	throw nie;
        } catch (Exception e) {
        	throw new NoSuccessException(e);
        }
	}
}
