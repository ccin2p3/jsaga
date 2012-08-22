package fr.in2p3.jsaga.adaptor.ourgrid.security;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import sun.awt.windows.ThemeReader;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;

/* ***************************************************
 * ***  Distributed Systems Lab(LSD)-UFCG) ***
 * ***   http://www.lsd.ufcg.edu.br        ***
 * ***************************************************
 * File:   OurGridDataAdaptor
 * Author: Patricia Alanis (patriciaam@lsd.ufcg.edu.br)
 * Date:   August 2012
 * ***************************************************/

public class OurGridSecurityCredencial extends  UserPassSecurityCredential implements SecurityCredential{

	/**
	 * Constructor of the OurGridSecurityCredencial class
	 * @param userId
	 * @param userPass
	 */
	public OurGridSecurityCredencial(String userId, String userPass) {

		super(userId, userPass);
	}

	/**
	 * Returns the value of the attribute (other than UserID)
	 * @param key
	 * @return value of the key attribute 
	 * @throws NotImplementedException
	 * @throws NoSuccessException
	 */
	public String getAttribute(String key) throws NotImplementedException,NoSuccessException {

		return super.getAttribute(key);
	}
}

