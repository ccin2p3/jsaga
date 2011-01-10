package fr.in2p3.jsaga.adaptor.arex.job;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.bes.BesUtils;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;

import org.oasis_open.docs.wsrf.rp_2.QueryExpressionType;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
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

	public String getType() {
        return "arex";
    }

	public int getDefaultPort() {
		return 2010;
	}

	public Class getJobClass() {
		return ArexJob.class;
	}

	protected Class getJobStatusClass() {
		return ArexJobStatus.class;
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
		
		System.out.println(getExecutionHosts("dd")[0]);
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        super.disconnect();
    }

	public Integer getExitCode(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat("YYYY-MM-dd'T'HH-mm-ss'Z'");
		try {
			return df.parse(getInfo(nativeJobId, "EndTime"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return new String[]{
				getInfo(nativeJobId, "ExecutionNode")
		};
	}
	
	// TODO : operation not permitted
	private String getInfo(String nativeJobId, String infoName) throws NotImplementedException, NoSuccessException {
        try {
        	String xpathQuery = "//glue:Services/glue:ComputingService/glue:ComputingEndpoint/glue:ComputingActivities/glue:ComputingActivity/glue:ID[contains(.,'98381294326045446690564')]/..";

        	QueryExpressionType query = new QueryExpressionType();
            query.setDialect(new URI("http://www.w3.org/TR/1999/REC-xpath-19991116"));
            MessageElement me = new MessageElement(new QName("XPathQuery"), xpathQuery);
            query.set_any(new MessageElement[]{me});

            System.out.println(BesUtils.dumpMessage("http://docs.oasis-open.org/wsrf/rpw-2", query));
    		/*QueryResourcePropertiesResponse*/ 
            //Object response = _arex_pt.queryResourceProperties(query);

            GetResourcePropertyDocumentResponse resp = _arex_pt.getResourcePropertyDocument();
    		
            //GetResourcePropertyResponse resp = _arex_pt.getResourceProperty(new QName("Contact"));

        /*} catch(java.rmi.RemoteException e) {
        	throw new NoSuccessException(e);
        } catch(UnknownQueryExpressionDialectFaultType e) {
        	throw new NotImplementedException(e);
        } catch (QueryEvaluationErrorFaultType e ) {
        	throw new NoSuccessException(e);
        } catch (InvalidQueryExpressionFaultType e) {
        	throw new NoSuccessException(e);
        } catch (InvalidResourcePropertyQNameFaultType e ) {
        	throw new NoSuccessException(e);*/
        } catch (ResourceUnknownFaultType e) {
        	throw new NotImplementedException(e);
        } catch (ResourceUnavailableFaultType e) {
        	throw new NotImplementedException(e);
        } catch (Exception e) {
        	throw new NoSuccessException(e);
        }
		return "";
	}
}
