package fr.in2p3.jsaga.adaptor.arex.job;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.arex.data.ArexHttpsDataAdaptor;
import fr.in2p3.jsaga.adaptor.bes.BesUtils;
import fr.in2p3.jsaga.adaptor.bes.job.BesJobMonitorAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobInfoAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;

import org.apache.axis.client.Stub;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.message.Text;
import org.apache.axis.types.URI;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityStatusType;
import org.nordugrid.schemas.arex.ARex_PortType;
import org.nordugrid.schemas.arex.ARex_ServiceLocator;

import org.oasis_open.docs.wsrf.rp_2.QueryExpressionType;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	private ArexHttpsDataAdaptor _data_adaptor;
	private static final Integer NB_TRIES = 10;
	
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
		
		/* used for getInfoXML
		_data_adaptor = new ArexHttpsDataAdaptor();
		_data_adaptor.setSecurityCredential(m_credential);
		_data_adaptor.connect(userInfo, host, port, basePath, attributes);
		*/
		
		//System.out.println(getExitCode("https://interop.grid.niif.hu:2010/arex-x509/1077212955322581395788565"));
		//String cr_string = getCreated("https://interop.grid.niif.hu:2010/arex-x509/1077212955322581395788565").toString();
		/*String cr_string = "Thu Jan 20 16:26:10 CET 2011";
		DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
	    Date creationTime;
		try {
			creationTime = df.parse(cr_string);
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	    System.out.println(creationTime.toString());*/

		/*
		System.out.println(getInfoWSRP("https://interop.grid.niif.hu:2010/arex-x509/1077212958849731186823177","Owner"));
		System.out.println(getInfoWSRP("https://interop.grid.niif.hu:2010/arex-x509/1077212958849731186823177","Efd"));
		throw new NoSuccessException("TO BE REMOVED");
		*/
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        //_data_adaptor = null;
        super.disconnect();
    }

	public Integer getExitCode(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return getExitCode(nativeJobId, NB_TRIES);
	}

	private Integer getExitCode(String nativeJobId, Integer nbTries) throws NotImplementedException, NoSuccessException {
		return Integer.parseInt(getInfo(nativeJobId, "ExitCode", nbTries));
	}
	
	public Date getCreated(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		throw new NotImplementedException();
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); // TODO : use joda-time http://mvnrepository.com/artifact/joda-time/joda-time/1.6.2
		try {
			return df.parse(getInfo(nativeJobId, "SubmissionTime", NB_TRIES).replaceAll("Z","UTC"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		try {
			return df.parse(getInfo(nativeJobId, "EndTime", NB_TRIES).replaceAll("Z","UTC"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return new String[]{
				getInfo(nativeJobId, "ExecutionNode", NB_TRIES)
		};
	}

	// Wrapper for getInfoXML or getInfoWSRF
	private String getInfo(String nativeJobId, String infoName, Integer nbTries) throws NotImplementedException, NoSuccessException {
		return getInfoWSRP( nativeJobId,  infoName, nbTries);
	}
	
	private String getInfoWSRP(String nativeJobId, String infoName, Integer nbTries) throws NotImplementedException, NoSuccessException {
        try {
    		int loop = 0;
    		while (loop < nbTries) {
	            SOAPHeaderElement she = new SOAPHeaderElement("http://www.w3.org/2005/08/addressing",
	            		"Action",
	            		"http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest");
	            ((Stub)_arex_pt).clearHeaders();
	            ((Stub)_arex_pt).setHeader(she);
	        	
	        	String xpathQuery = "//glue:Services/glue:ComputingService/glue:ComputingEndpoint/glue:ComputingActivities/glue:ComputingActivity/glue:IDFromEndpoint[.='" + nativeJobId + "']/../glue:" + infoName;
	
	        	QueryExpressionType query = new QueryExpressionType();
	            query.setDialect(new URI("http://www.w3.org/TR/1999/REC-xpath-19991116"));
	            MessageElement me = new MessageElement(new Text(xpathQuery));
	            query.set_any(new MessageElement[]{me});
	
	            QueryResourcePropertiesResponse response = _arex_pt.queryResourceProperties(query);
	            
	            if (response != null) {
	            	/* loop for ComputingActivity node
					for (MessageElement grpr_elmt: response.get_any()) {
						if (infoName.equals(grpr_elmt.getName())) {
							return grpr_elmt.getFirstChild().getNodeValue();
						}
					}*/
	            	/* for direct acces to infoName */
	            	return response.get_any()[0].getAsString();
	            }
				loop++;
				// Do not sleep at last attempty
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

	/*
	 * Obsolete, getInfoWSRF is used instead
	 */
	private String getInfoXML(String nativeJobId, String infoName) throws NotImplementedException, NoSuccessException {
		try {
			int loop = 0;
			while (loop < 10) {
			  InputStream _info_xml = _data_adaptor.getInputStream(_bes_url.getPath(), "info");
			  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			  DocumentBuilder db = dbf.newDocumentBuilder();
			  Document doc;
			  try {
				  doc = db.parse(_info_xml);
				  doc.getDocumentElement().normalize();
				  NodeList caLst = doc.getElementsByTagName("ComputingActivity");
				  for (int s = 0; s < caLst.getLength(); s++) {
					  Node ca = caLst.item(s);
					  if (ca.getNodeType() == Node.ELEMENT_NODE) {
						  Element fstElmnt = (Element) ca;
						  String id = fstElmnt.getElementsByTagName("IDFromEndpoint").item(0).getFirstChild().getNodeValue();
						  if (id.equals(nativeJobId)) {
							  //if (infoName.equals("CreationTime")) {
								//  return fstElmnt.getAttribute(infoName);
							  //} else {
								  Node val = fstElmnt.getElementsByTagName(infoName).item(0);
								  if (val == null) { // Node is not in XML doc yet, needs refresh
									  loop++;
									  break;
								  }
								  return val.getFirstChild().getNodeValue();
							  //}
						  }
					  }
				  }
			  } catch (SAXException e) { // Sometimes : 13014:2: XML document structures must start and end within the same entity.
				  loop++;
			  }
			  Thread.sleep(5000);
			}
			throw new NoSuccessException("Not found: " + infoName);
		} catch (PermissionDeniedException e) {
			throw new NoSuccessException(e);
		} catch (BadParameterException e) {
			throw new NoSuccessException(e);
		} catch (DoesNotExistException e) {
			throw new NotImplementedException(e);
		} catch (TimeoutException e) {
			throw new NoSuccessException(e);
		} catch (IOException e) {
			throw new NoSuccessException(e);
		} catch (ParserConfigurationException e) {
			throw new NoSuccessException(e);
		} catch (InterruptedException e) {
			throw new NoSuccessException(e);
		}
	}
	
	protected JobStatus instanciateJobStatusObject(String nativeJobId, ActivityStatusType ast) throws NoSuccessException {
		try {
			Integer exCode = getExitCode(nativeJobId, 1);
			return new ArexJobStatus(nativeJobId, ast, exCode);
		} catch (NotImplementedException nie) {
			return new ArexJobStatus(nativeJobId, ast);
		}
	}
 
}
