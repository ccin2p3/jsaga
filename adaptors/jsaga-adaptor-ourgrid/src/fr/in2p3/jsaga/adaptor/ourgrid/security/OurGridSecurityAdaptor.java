package fr.in2p3.jsaga.adaptor.ourgrid.security;

import java.util.Map;

import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.ourgrid.job.OurGridConstants;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/


public class OurGridSecurityAdaptor implements SecurityAdaptor {

	private final String USER_ID = "UserID";
	private final String USER_NAME = "user.name";
	private final String USER_PASS = "UserPass";

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {

		return new Default[]{new Default(USER_ID, System.getProperty(USER_NAME))};
	}

	public String getType() {

		return OurGridConstants.TYPE_ADAPTOR;
	}

	public Usage getUsage() {

		return new UAnd(new Usage[] {  new U(USER_ID), new U(USER_PASS) });
	}

	public SecurityCredential createSecurityCredential(int usage, Map attributes,String contextId) 
			throws IncorrectStateException, TimeoutException,NoSuccessException {

		String userPass = null;

		if (attributes.containsKey(USER_PASS)) {

			userPass = (String)attributes.get(USER_PASS);
		}
		
		return new OurGridSecurityCredencial( (String)attributes.get(USER_ID),userPass);	  
	}


	public Class getSecurityCredentialClass() {

		return OurGridSecurityCredencial.class;
	}


}