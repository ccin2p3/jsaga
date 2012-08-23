package fr.in2p3.jsaga.adaptor.ourgrid.security;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

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

	public OurGridSecurityCredencial(String userId, String userPass) {

		super(userId, userPass);
	}

	public String getAttribute(String key) throws NotImplementedException,NoSuccessException {

		return super.getAttribute(key);
	}
}

