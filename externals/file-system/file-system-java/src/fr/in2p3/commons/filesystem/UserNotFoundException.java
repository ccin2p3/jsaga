package fr.in2p3.commons.filesystem;

import java.security.InvalidParameterException;

public class UserNotFoundException extends InvalidParameterException {

	public UserNotFoundException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9218281447261429949L;

}
