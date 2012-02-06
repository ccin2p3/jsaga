package fr.in2p3.jsaga.adaptor.security;

public class GnomeKeyringSecurityAdaptor extends SecretServiceSecurityAdaptor {

	public String getType() {
		return "gnome-keyring";
	}

	@Override
	protected String getDefaultCollection() {
		return "login";
	}

	
}
