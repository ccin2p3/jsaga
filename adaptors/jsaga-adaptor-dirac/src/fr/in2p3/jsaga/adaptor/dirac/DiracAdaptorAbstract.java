package fr.in2p3.jsaga.adaptor.dirac;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracConstants;
import fr.in2p3.jsaga.adaptor.dirac.util.DiracRESTClient;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DiracJobAdaptorAbstract
 * Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
 * Date:   30 sept 2013
 * ***************************************************/

public abstract class DiracAdaptorAbstract implements ClientAdaptor {

	protected Logger m_logger = Logger.getLogger(this.getClass());

	protected X509SecurityCredential m_credential;
	protected URL m_url;
	protected String m_accessToken;
	
	public Class[] getSupportedSecurityCredentialClasses() {
		return new Class[]{X509SecurityCredential.class};
	}

	public void setSecurityCredential(SecurityCredential credential) {
		m_credential = (X509SecurityCredential) credential;
	}

	public int getDefaultPort() {
		// TODO default port
		return 0;
	}

	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {
		try {
			m_url = new URL("https",host, port, "/");
		} catch (MalformedURLException e) {
			throw new NoSuccessException(e);
		}
	}

	public void disconnect() throws NoSuccessException {
	}

}