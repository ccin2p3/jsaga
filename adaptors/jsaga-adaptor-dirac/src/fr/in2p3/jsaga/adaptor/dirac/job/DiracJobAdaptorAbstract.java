package fr.in2p3.jsaga.adaptor.dirac.job;

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

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.dirac.DiracAdaptorAbstract;
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

// TODO: autorefresh when token has expired
public class DiracJobAdaptorAbstract extends DiracAdaptorAbstract implements JobAdaptor {

	private final static String P_GROUP = "Group";
	private final static String P_SETUP = "Setup";
	private final static String P_SITES = "Sites";
	
	protected String[] m_sites;
	
	public void connect(String userInfo, String host, int port,
			String basePath, Map attributes) throws NotImplementedException,
			AuthenticationFailedException, AuthorizationFailedException,
			IncorrectURLException, BadParameterException, TimeoutException,
			NoSuccessException {
		super.connect(userInfo, host, port, basePath, attributes);
//		try {
//			m_url = new URL("https",host, port, "/");
//		} catch (MalformedURLException e) {
//			throw new NoSuccessException(e);
//		}
        JSONObject resultDict;

        // Get the group
        String group = (attributes.containsKey(P_GROUP))?
        		(String)attributes.get(P_GROUP):
        		getTokenMetadata(P_GROUP);
        		
        // Get the setup
        String setup = (attributes.containsKey(P_SETUP))?
        		(String)attributes.get(P_SETUP):
        		getTokenMetadata(P_SETUP);
        
    	// get the Dirac token
		try {
			DiracRESTClient client = new DiracRESTClient(m_credential);
	        // GET request parameters
			client.addParam(DiracConstants.DIRAC_GET_PARAM_GRANT_TYPE, DiracConstants.DIRAC_GRANT_TYPE_CLIENT_CREDENTIALS);
			client.addParam(DiracConstants.DIRAC_GET_PARAM_GROUP, group);
			client.addParam(DiracConstants.DIRAC_GET_PARAM_SETUP, setup);
			resultDict = client.get(new URL(m_url, DiracConstants.DIRAC_PATH_TOKEN));
	    	m_accessToken = (String) resultDict.get(DiracConstants.DIRAC_GET_RETURN_TOKEN);
			m_logger.debug("Access Token:" + m_accessToken);
		} catch (Exception e) {
			throw new NoSuccessException(e);
		}

		// Read preferred sites from config
		if (attributes.containsKey(P_SITES)) {
			// TODO support list (vector)
			m_sites = new String[]{(String)attributes.get(P_SITES)};
		}
//		m_sites = new String[]{"LCG.CNAF.it"};
	}

	public void disconnect() throws NoSuccessException {
	}

	public String getType() {
		return "dirac";
	}

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOptional(P_GROUP),
                new UOptional(P_SETUP)
        });
    }


	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
		return null;
	}

	/**
	 * get the "Group" or "Setup" associated with a user, when it is not specified in the context
	 * the method returns the value if it is the only one.
	 * @param param "Group" or "Setup
	 * @return the group or the setup
	 * @throws BadParameterException if no value was found or if more than 1 value was found
	 * @throws IncorrectURLException
	 * @throws NoSuccessException
	 * @throws AuthenticationFailedException
	 */
	private String getTokenMetadata(String param) throws BadParameterException, IncorrectURLException, 
								NoSuccessException, AuthenticationFailedException {
		String path;
		String key;
		if (P_GROUP.equals(param)) {
			path = DiracConstants.DIRAC_PATH_GROUPS;
			key = "groups";
		} else if (P_SETUP.equals(param)) {
			path = DiracConstants.DIRAC_PATH_SETUPS;
			key = "setups";
		} else {
			throw new BadParameterException("Unknown parameter:" + param);
		}
		JSONObject resultDict;
		try {
			resultDict = new DiracRESTClient(m_credential).get(new URL(m_url, path));
		} catch (MalformedURLException e1) {
			throw new IncorrectURLException(e1);
		}
		if (!resultDict.containsKey(key)) {
			throw new BadParameterException("Missing parameter: " + param + ". Could not get it from Dirac");
		}
		JSONArray list = (JSONArray)resultDict.get(key);
		m_logger.debug(list.toJSONString());
		if (list.size() != 1) {
			throw new BadParameterException("Missing parameter: " + param + ". Could not get it from Dirac");
		}
		return (String)list.get(0);
				
	}
	
	/**
	 * get the Job information from Dirac
	 * @param nativeJobId
	 * @return the Job info in JSON format
	 * @throws NoSuccessException
	 * @throws AuthenticationFailedException
	 * @throws IncorrectURLException
	 * @throws MalformedURLException
	 */
	protected JSONObject getJob(String nativeJobId) throws NoSuccessException, AuthenticationFailedException, 
															IncorrectURLException, MalformedURLException {
		if (m_accessToken == null) {
			throw new NoSuccessException("Need a token first");
		}
		return new DiracRESTClient(m_credential, m_accessToken)
						.get(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS + "/" + nativeJobId));
	}
	
	/**
	 * get the list of jobs from Dirac
	 * @return the list of jobs in JSON format
	 * @throws NoSuccessException
	 * @throws AuthenticationFailedException
	 * @throws IncorrectURLException
	 * @throws MalformedURLException
	 */
	protected JSONArray getJobs()  throws NoSuccessException, AuthenticationFailedException, 
															IncorrectURLException, MalformedURLException {
		return getJobs(new JSONObject());
	}
	
	/**
	 * get the list of jobs from Dirac that match arguments
	 * @param args to match
	 * @return the list of jobs in JSON format
	 * @throws NoSuccessException
	 * @throws AuthenticationFailedException
	 * @throws IncorrectURLException
	 * @throws MalformedURLException
	 */
	protected JSONArray getJobs(JSONObject args)  throws NoSuccessException, AuthenticationFailedException, 
															IncorrectURLException, MalformedURLException {
		if (m_accessToken == null) {
			throw new NoSuccessException("Need a token first");
		}
		DiracRESTClient client = new DiracRESTClient(m_credential, m_accessToken);
		client.addParam(args);
		return (JSONArray) client.get(new URL(m_url, DiracConstants.DIRAC_PATH_JOBS)).get("jobs");
		
	}
}