package fr.in2p3.jsaga.adaptor.unicore.job;

import java.util.Map;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import fr.in2p3.jsaga.adaptor.unicore.UnicoreAbstract;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.job.monitor.JobStatus;
import fr.in2p3.jsaga.adaptor.job.monitor.QueryIndividualJob;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UnicoreJobMonitorAdaptor
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   01/09/2011
* ***************************************************/
public class UnicoreJobMonitorAdaptor extends UnicoreAbstract implements
		QueryIndividualJob {

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
    	return new Default[]{
    			new Default(TARGET, "DEMO-SITE"),
    			new Default(SERVICE_NAME, "Registry"), 
    			new Default(RES, "default_registry"),
    			};
    }

	/*public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			BadParameterException, TimeoutException,
			NoSuccessException {
		// TODO Auto-generated method stub

	}*/

	public JobStatus getStatus(String nativeJobId) throws TimeoutException,
			NoSuccessException {
		try {
			EndpointReferenceType _epr = EndpointReferenceType.Factory.newInstance();
		    _epr.addNewAddress().setStringValue(nativeJobId);
			UnicoreJob uj = new UnicoreJob(_epr, m_uassecprop);
			return uj.getStatus();
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}
	}

}
