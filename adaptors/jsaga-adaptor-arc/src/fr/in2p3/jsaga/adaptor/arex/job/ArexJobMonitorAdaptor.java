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
		_data_adaptor = new ArexHttpsDataAdaptor();
		_data_adaptor.setSecurityCredential(m_credential);
		_data_adaptor.connect(userInfo, host, port, basePath, attributes);
		
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
	    System.out.println(creationTime.toString());

		throw new NoSuccessException("TO BE REMOVED");*/
    }

	public void disconnect() throws NoSuccessException {
        _arex_pt = null;
        _data_adaptor = null;
        super.disconnect();
    }

	public Integer getExitCode(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return Integer.parseInt(getInfo(nativeJobId, "ExitCode"));
	}

	public Date getCreated(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		throw new NotImplementedException();
	}

	public Date getStarted(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); // TODO : use org.w3c.util.DateParser
		try {
			return df.parse(getInfo(nativeJobId, "SubmissionTime").replaceAll("Z","UTC"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	}

	public Date getFinished(String nativeJobId) throws NotImplementedException,	NoSuccessException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		try {
			return df.parse(getInfo(nativeJobId, "EndTime").replaceAll("Z","UTC"));
		} catch (ParseException e) {
			throw new NoSuccessException(e);
		}
	}

	public String[] getExecutionHosts(String nativeJobId)	throws NotImplementedException, NoSuccessException {
		return new String[]{
				getInfo(nativeJobId, "ExecutionNode")
		};
	}
	
	private String getInfo(String nativeJobId, String infoName) throws NotImplementedException, NoSuccessException {
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
	
	// FIXME : operation not permitted
	private String getInfoWSRP(String nativeJobId, String infoName) throws NotImplementedException, NoSuccessException {
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
