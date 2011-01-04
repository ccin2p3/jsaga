package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.bes.BesUtils;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;

import org.ggf.schemas.bes.x2006.x08.besFactory.BESFactoryPortType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BasicResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BesFactoryServiceLocator;
import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import java.util.Map;

import javax.xml.rpc.ServiceException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

/**
 * This class is the abstract class for the Adaptor specific to a BES implementation
 */
public abstract class BesJobAdaptorAbstract implements BesClientAdaptor {

	protected static final String BES_FACTORY_PORT_TYPE = "BESFactoryPortType";
	
	protected String _bes_url ;
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
    
    public Class[] getSupportedSecurityCredentialClasses() {
    	return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    		m_credential = (JKSSecurityCredential) credential;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	_bes_url = getBESUrl(host, port, basePath, attributes);
    	if (_bes_pt != null) return;
    	
        BesFactoryServiceLocator _bes_service = new BesFactoryServiceLocator();
		try {
			_bes_service.setEndpointAddress(BES_FACTORY_PORT_TYPE, _bes_url);
	        _bes_pt=(BESFactoryPortType) _bes_service.getBESFactoryPortType();
		} catch (ServiceException e) {
			throw new NoSuccessException(e);
		}
		try {
			GetFactoryAttributesDocumentResponseType r = _bes_pt.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
	        FactoryResourceAttributesDocumentType attr = r.getFactoryResourceAttributesDocument();
			_br = attr.getBasicResourceAttributesDocument();
			//_cr = attr.getContainedResource();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }

	public void disconnect() throws NoSuccessException {
        m_credential = null;
        _bes_pt = null;
        _br = null;
        _cr = null;
    }    

	///////////////////////////////////
    // Implementation of BesClientAdaptor
	///////////////////////////////////
    
	/**
	 * Get the BES URL to use
	 * 
	 * @param host
	 * @param port
	 * @param basePath
	 * @param attributes
	 * @return String the URL build as "https://"+host+":"+port+basePath
	 */
    public String getBESUrl(String host, int port, String basePath, Map attributes) {
    	return "https://"+host+":"+port+basePath;
    }
    
	///////////////////////////////////
	// Other public methods
	///////////////////////////////////
	
    public String activityId2NativeId(EndpointReferenceType epr) throws NoSuccessException {
		BesJob _job;
		try {
			_job = (BesJob) getJobClass().newInstance();
		} catch (InstantiationException e) {
			throw new NoSuccessException(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccessException(e);
		}
		_job.setActivityIdentifier(epr);
		return _job.getNativeJobID();    	
    }

    public EndpointReferenceType nativeId2ActivityId(String nativeId) throws NoSuccessException {
		BesJob _job;
		try {
			_job = (BesJob) getJobClass().newInstance();
		} catch (InstantiationException e) {
			throw new NoSuccessException(e);
		} catch (IllegalAccessException e) {
			throw new NoSuccessException(e);
		}
		_job.setNativeJobId(nativeId);
		return _job.getActivityIdentifier();   	
    }

}
