package fr.in2p3.jsaga.adaptor.ourgrid.job;

import java.util.Map;

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
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.ourgrid.security.OurGridSecurityCredencial;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/
/**
 * 
 * @author patriciaam
 *
 */
public abstract class OurGridAbstract implements ClientAdaptor {

	protected String m_account;
	protected String m_passPhrase;

	/**
	 *  Gets the defaults values for (some of the) attributes supported by this adaptor 
	 *  These values can be static or dynamically created from the information available on local host
	 *  (environment variables, files, ...) and from the attributes map.
	 *  @param attributes the attributes set by the user
	 *  @return Returns an array of default values
	 */
	public Default[] getDefaults(Map attributes)throws IncorrectStateException {

		return null;
	}

	/**
	 * @return Returns the adaptor type
	 */
	public String getType() {

		return OurGridConstants.TYPE_ADAPTOR;
	}

	/**
	 * 
	 * @return userID used to login to the server
	 */
	public String getM_account() {
		return m_account;
	}

	/**
	 * Sets the userId used to login to the server
	 * @param m_account userID
	 */
	public void setM_account(String m_account) {
		this.m_account = m_account;
	}

	/**
	 * 
	 * @return password used to login to the server
	 */
	public String getM_passPhrase() {
		return m_passPhrase;
	}

	/**
	 * Sets the password used to login to the server
	 * @param m_passPhrase password
	 */
	public void setM_passPhrase(String m_passPhrase) {
		this.m_passPhrase = m_passPhrase;
	}

	/**
	 * Gets a data structure that describes how to use this adaptor
	 * This data structure contains attribute names with usage constraints 
	 * (and/or,required/optional, hidden...)
	 * @return Returns the usage data structure
	 * */
	public Usage getUsage() {

		return null;
	}

	/**
	 * Connects to the server and initialize the connection with the
	 * provided attributes
	 * 
	 * @param userInfo the user login
	 * @param host the server
	 * @param port the port
	 * @param basePath the base path
	 * @param attributes the provided attributes
	 */
	public void connect(String userInfo, String host, int port,	String basePath, Map attributes)
			throws NotImplementedException,	AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {


	}

	/**
	 * This method disconnect from the server
	 */
	public void disconnect() throws NoSuccessException {

	}

	/**
	 * @return Returns the port number
	 */
	public int getDefaultPort() {

		return OurGridConstants.PORT;

	}

	/**
	 * Returns the array of supported SecurityCredential classes
	 */
	public Class[] getSupportedSecurityCredentialClasses() {

		return new Class[] { OurGridSecurityCredencial.class };
	}

	/**
	 * Set the security credential used for the security context
	 * @param credential the security credential
	 */
	public void setSecurityCredential(SecurityCredential credential) {

		setM_account(((OurGridSecurityCredencial) credential).getUserID());
		setM_passPhrase(((OurGridSecurityCredencial) credential).getUserPass());

	}
}