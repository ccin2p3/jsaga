package fr.in2p3.jsaga.adaptor.bes.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.JKSSecurityCredential;
//import fr.in2p3.jsaga.adaptor.u6.U6SecurityManagerImpl;
//import fr.in2p3.jsaga.adaptor.u6.TargetSystemInfo;
//import fr.in2p3.jsaga.adaptor.u6.U6Abstract;

import org.ggf.schemas.bes.x2006.x08.besFactory.BESFactoryPortType;
import org.ggf.schemas.bes.x2006.x08.besFactory.BesFactoryServiceLocator;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import java.util.List;
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

public abstract class BesJobAdaptorAbstract implements ClientAdaptor {

	protected static final String APPLICATION_NAME = "ApplicationName";
	protected static final String BES_FACTORY_PORT_TYPE = "BESFactoryPortType";
	
	//protected String m_serviceName;
	//protected String m_applicationName;
	protected String _bes_url ;
	protected JKSSecurityCredential m_credential;
	//protected U6SecurityManagerImpl m_securityManager = null;
    
	//protected BesFactoryServiceLocator _bes_service;
	protected BESFactoryPortType _bes_pt;
	
    public String getType() {
        return "bes";
    }
    
    public Class[] getSupportedSecurityCredentialClasses() {
    	return new Class[]{JKSSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    	 m_credential = (JKSSecurityCredential) credential;
    }

    public int getDefaultPort() {
        return 8080;
    }

	public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    	_bes_url = "https://"+host+":"+port+basePath;
        
    	// get APPLICATION_NAME
    	//m_applicationName = (String) attributes.get(APPLICATION_NAME);

    	BesFactoryServiceLocator _bes_service = new BesFactoryServiceLocator();
		try {
			_bes_service.setEndpointAddress(BES_FACTORY_PORT_TYPE,
							_bes_url + "/BESFactory?res=default_bes_factory");
	        _bes_pt=(BESFactoryPortType) _bes_service.getBESFactoryPortType(); 
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new NoSuccessException(e);
		}
		
    }

	public void disconnect() throws NoSuccessException {
        //m_serverUrl = null;
        m_credential = null;
        //m_applicationName = null;
        //m_securityManager = null;
        _bes_pt = null;
    }    
	

    /*
	protected JobClient getJobById(String nativeJobId) throws NoSuccessException {
		try {		
	    	// TODO Optimize this
	        // list jobs
			TargetSystemInfo targetSystemInfo = findTargetSystem();
	        List<JobClient> jobList = targetSystemInfo.getTargetSystem().getJobs();
	        for (JobClient jobClient : jobList) {
	        	String currentJobId = ((AtomicJobClientImpl) jobClient).getId().toString();
	        	if(currentJobId.equals(nativeJobId)) {
					return jobClient;
				}
			}
		} catch (GPEInvalidResourcePropertyQNameException e) {
			throw new NoSuccessException(e);
		} catch (GPEResourceUnknownException e) {
			throw new NoSuccessException(e);
		} catch (GPEUnmarshallingException e) {
			throw new NoSuccessException(e);
		} catch (GPEMiddlewareRemoteException e) {
			throw new NoSuccessException(e);
		} catch (GPEMiddlewareServiceException e) {
			throw new NoSuccessException(e);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
        throw new NoSuccessException("Unable to get job:"+nativeJobId);
	}
	*/
 }
