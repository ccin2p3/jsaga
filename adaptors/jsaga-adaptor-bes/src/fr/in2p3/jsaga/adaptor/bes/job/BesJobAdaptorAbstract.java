package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.ggf.schemas.bes.x2006.x08.besFactory.BESFactoryPortType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BasicResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BesFactoryServiceLocator;
import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.generated.org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.SOAPException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

/**
 * This class is the generic class to access a BES service
 */
public abstract class BesJobAdaptorAbstract implements BesClientAdaptor {

	protected static final String ATTR_REF_PARAM_NS = "ReferenceParameterNS";
	protected static final String ATTR_REF_PARAM_NAME = "ReferenceParameterName";
	protected static final String ATTR_REF_PARAM_VALUE = "ReferenceParameterValue";
	
	protected URI _bes_url ;
	protected JKSSecurityCredential m_credential;

	protected BESFactoryPortType _bes_pt = null;
	
	// Basic resources
	protected BasicResourceAttributesDocumentType _br = null;
	
	// Contained resources
	// Can be of type BasicResourceAttributesDocumentType or FactoryResourceAttributesDocumentType
	protected Object[] _cr = null;

	//////////////////////////////////////////////////
	// Implementation of the ClientAdaptor interface
	//////////////////////////////////////////////////
	
    public Usage getUsage() {
    	return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{};
    }
    
    public String getType() {
        return "bes";
    }

	public int getDefaultPort() {
		return 8443;
	}

    public Class[] getSupportedSecurityCredentialClasses() {
    	return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    		m_credential = (JKSSecurityCredential) credential;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	try {
			_bes_url = getBESUrl(host, port, basePath, attributes);
		} catch (URISyntaxException e) {
			throw new NoSuccessException(e);
		}
		Logger.getLogger(BesJobAdaptorAbstract.class).info("Connecting to BES service at: " + _bes_url);
    	if (_bes_pt != null) return;
    	
        BesFactoryServiceLocator _bes_service = new BesFactoryServiceLocator();
		try {
			_bes_service.setBESFactoryPortTypeEndpointAddress(_bes_url.toString());
	        _bes_pt= _bes_service.getBESFactoryPortType();
	        
			String referenceParameter = (String) attributes.get(ATTR_REF_PARAM_VALUE);
	        if (referenceParameter != null) {
	        	String nameSpace = (String) attributes.get(ATTR_REF_PARAM_NS);
	        	String name = (String) attributes.get(ATTR_REF_PARAM_NAME);
	        	if (nameSpace == null || name == null)
	        		throw new NoSuccessException("Missing ReferenceParameter attributes");
	        	// add reference parameter
	            org.apache.axis.message.SOAPHeaderElement ref = new org.apache.axis.message.SOAPHeaderElement(
	            		new org.apache.axis.message.PrefixedQName(nameSpace,name, "refparam"));
	            ref.setMustUnderstand(false);
	            ref.addAttribute(EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI(), "IsReferenceParameter", "true");
	            ref.setObjectValue(referenceParameter);
	        	((org.apache.axis.client.Stub) _bes_pt).setHeader(ref);
	        }
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
		// TODO : uncomment to check resources
		/*try {
	        FactoryResourceAttributesDocumentType attr = getBESAttributes();
			_br = attr.getBasicResourceAttributesDocument();
			//_cr = attr.getContainedResource();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
    }

	public void disconnect() throws NoSuccessException {
        m_credential = null;
       _bes_pt = null;
        _bes_url = null;
        _br = null;
        _cr = null;
    }    

	///////////////////////////////////
    // Implementation of BesClientAdaptor
	///////////////////////////////////
    
	public BesJob getJob() {
		return new BesJob();
	}

	/**
	 * Get the BES URL to use
	 * 
	 * @param host
	 * @param port
	 * @param basePath
	 * @param attributes
	 * @return the URL build as "https://"+host+":"+port+basePath+"?"+list_of_attributes
	 * @throws URISyntaxException 
	 */
    public URI getBESUrl(String host, int port, String basePath, Map attributes) throws URISyntaxException {
    	String uri = "https://"+host+":"+port+basePath+"?";
    	Iterator iter = attributes.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry me = ((Map.Entry)iter.next());
    		if (!me.getKey().equals("CheckAvailability")
    				&& !me.getKey().equals(ATTR_REF_PARAM_NS)
    				&& !me.getKey().equals(ATTR_REF_PARAM_NAME)
    				&& !me.getKey().equals(ATTR_REF_PARAM_VALUE)){
        		uri += me.getKey() + "=" + me.getValue()+"&";
    		}
    	}
    	// remove trailing char (either & or ?)
    	uri=uri.substring(0, uri.length()-1);
    	return new URI(uri); 
    }
    
	///////////////////////////////////
	// Other public methods
	///////////////////////////////////
	
    public String activityId2NativeId(EndpointReferenceType epr, boolean store) throws NoSuccessException {
		BesJob _job;
		_job = getJob();
		_job.setActivityId(epr, store);
		return _job.getNativeId();
    }
    public String activityId2NativeId(EndpointReferenceType epr) throws NoSuccessException {
    	return this.activityId2NativeId(epr, false);
    }
    
    public EndpointReferenceType nativeId2ActivityId(String nativeId) throws NoSuccessException {
		BesJob _job;
		_job = getJob();
		_job.setNativeId(nativeId);
		return _job.getActivityIdentifier();
    }

	public FactoryResourceAttributesDocumentType getBESAttributes() throws NoSuccessException {
		try {
			org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType gfadrt = _bes_pt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
			Logger.getLogger(BesJobAdaptorAbstract.class).debug(fr.in2p3.jsaga.adaptor.bes.BesUtils.dumpBESMessage(gfadrt));
			return gfadrt.getFactoryResourceAttributesDocument();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}
	
	/////////////////////////
	// Private methods
	/////////////////////////
	
	
    /////////////////////////
    // OLD deprecated methods
    /////////////////////////
	/**
	 * @deprecated
	 */
//	private SOAPHeaderElement buildUsernamePassword() throws Exception {
//	    String secextNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
//        WSSecUsernameToken token = new WSSecUsernameToken();
//        // USELESS token.setPasswordsAreEncoded(false);
//        token.setPasswordType(WSConstants.PASSWORD_TEXT);
//        // TODO: support of user/password adaptor ?
//        token.setUserInfo("indiaInterop", "XXXX");
//        // USELESS token.addCreated();
//        SOAPHeaderElement wsseSecurity = new SOAPHeaderElement(new org.apache.axis.message.PrefixedQName(secextNS,"Security", "wsse"));
//        Document doc = wsseSecurity.getAsDocument();
//        WSSecHeader secHeader = new WSSecHeader();
//        secHeader.setMustUnderstand(false);
//        secHeader.setActor("http://schemas.xmlsoap.org/soap/actor/next");
//        secHeader.insertSecurityHeader(doc);
//        token.prepare(doc);
//        token.prependToHeader(secHeader);
//        
//        return new SOAPHeaderElement(secHeader.getSecurityHeader());
//	}
	
	/**
	 * @deprecated
	 */
	private SOAPHeaderElement buildBinaryToken() throws SOAPException {
	    String secextNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	    String utilityNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        org.apache.axis.message.SOAPHeaderElement wsseSecurity = new org.apache.axis.message.SOAPHeaderElement(new org.apache.axis.message.PrefixedQName(secextNS,"SecurityTokenReference", "wsse"));
        org.apache.axis.message.MessageElement secToken = new org.apache.axis.message.MessageElement(secextNS, "wsse:BinarySecurityToken");
        secToken.addAttribute(null, "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1");
        secToken.addAttribute("wsu", utilityNS, "Id", "RecipientMessageIdentity");
        secToken.setObjectValue("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");
        wsseSecurity.appendChild(secToken);
        return wsseSecurity;
	}
	
	/**
	 * @deprecated
	 */
//	private SOAPHeaderElement buildEncryptedKey() throws Exception {
//	    String secextNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
//		WSSecEncryptedKey token = new WSSecEncryptedKey();
//		token.setUseThisCert(m_credential.getCertificate()); 
//		token.setUserInfo(m_credential.getUserID());
//        SOAPHeaderElement wsseSecurity = new SOAPHeaderElement(new org.apache.axis.message.PrefixedQName(secextNS,"Security", "wsse"));
//        WSSecHeader secHeader = new WSSecHeader();
//        Document doc = wsseSecurity.getAsDocument();
//        secHeader.setMustUnderstand(true);
//        secHeader.setActor("http://schemas.xmlsoap.org/soap/actor/next");
//        secHeader.insertSecurityHeader(doc);
//        token.prepare(doc, new CertificateStore(new X509Certificate[]{m_credential.getCertificate()}));
//        token.appendToHeader(secHeader);
//        return new SOAPHeaderElement(secHeader.getSecurityHeader());
//	}

	/**
	 * @deprecated
	 */
	private SOAPHeaderElement buildUsernamePasswordDeprecated() throws SOAPException {
	    String secextNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	    String utilityNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        org.apache.axis.message.SOAPHeaderElement wsseSecurity = new org.apache.axis.message.SOAPHeaderElement(new org.apache.axis.message.PrefixedQName(secextNS,"Security", "wsse"));
        wsseSecurity.setActor(null);
        wsseSecurity.setMustUnderstand(true);
        org.apache.axis.message.MessageElement usernameToken = new org.apache.axis.message.MessageElement(secextNS, "wsse:UsernameToken");
        usernameToken.addAttribute("wsu", utilityNS, "Id", "UsernameToken-1");
        org.apache.axis.message.MessageElement username = new org.apache.axis.message.MessageElement(secextNS, "wsse:Username");
        org.apache.axis.message.MessageElement password = new org.apache.axis.message.MessageElement(secextNS, "wsse:Password");
        username.setObjectValue("indiaInterop");
        usernameToken.addChild(username);
        password.addAttribute(null, secextNS, "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
        password.setObjectValue("XXXXX");
        usernameToken.addChild(password);
        wsseSecurity.appendChild(usernameToken);
		return wsseSecurity;
	}


}
