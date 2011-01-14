package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.job.BadResource;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslator;
import fr.in2p3.jsaga.adaptor.job.control.description.JobDescriptionTranslatorJSDL;
import fr.in2p3.jsaga.adaptor.security.GlobusSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
//import fr.in2p3.jsaga.adaptor.security.VOMSSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
//import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;

import org.apache.axis.message.MessageElement;
import org.ggf.schemas.bes.x2006.x08.besFactory.ActivityDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BESFactoryPortType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BesFactoryServiceLocator;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.CreateActivityType;
import org.ggf.schemas.bes.x2006.x08.besFactory.FactoryResourceAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.GetFactoryAttributesDocumentType;
import org.ggf.schemas.bes.x2006.x08.besFactory.InvalidRequestMessageFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAcceptingNewActivitiesFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.NotAuthorizedFaultType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivitiesType;
import org.ggf.schemas.bes.x2006.x08.besFactory.TerminateActivityResponseType;
import org.ggf.schemas.bes.x2006.x08.besFactory.UnsupportedFeatureFaultType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinition_Type;
import org.globus.wsrf.encoding.DeserializationException;
import org.globus.wsrf.encoding.ObjectDeserializer;
import org.globus.wsrf.encoding.ObjectSerializer;
import org.globus.wsrf.encoding.SerializationException;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BesJobAdaptorAbstract
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   23 Nov. 2010
* ***************************************************/

public abstract class BesJobAdaptorAbstract implements ClientAdaptor {

	protected static final String BES_FACTORY_PORT_TYPE = "BESFactoryPortType";
	
	protected String _bes_url ;
	protected JKSSecurityCredential m_credential;

	protected BESFactoryPortType _bes_pt = null;
	
    /*public String getType() {
        return "bes";
    }*/
    
	protected abstract Class getJobClass();
	
    public Class[] getSupportedSecurityCredentialClasses() {
    	return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    		m_credential = (JKSSecurityCredential) credential;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	_bes_url = "https://"+host+":"+port+basePath;
    	// ?res=default_bes_factory in case of Unicore
		if (attributes.get("res") != null) {
			_bes_url += "?res=" + (String)attributes.get("res");
		}
        System.out.println(_bes_url);
    	if (_bes_pt != null) return;
    	
        BesFactoryServiceLocator _bes_service = new BesFactoryServiceLocator();
		try {
			_bes_service.setEndpointAddress(BES_FACTORY_PORT_TYPE, _bes_url);
	        _bes_pt=(BESFactoryPortType) _bes_service.getBESFactoryPortType();
		} catch (ServiceException e) {
			throw new NoSuccessException(e);
		}
		
    }

	public void disconnect() throws NoSuccessException {
        m_credential = null;
        _bes_pt = null;
    }    
   	
    public JobDescriptionTranslator getJobDescriptionTranslator() throws NoSuccessException {
        return new JobDescriptionTranslatorJSDL();
    }

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
