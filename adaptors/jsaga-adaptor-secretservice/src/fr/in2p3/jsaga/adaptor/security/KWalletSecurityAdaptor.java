package fr.in2p3.jsaga.adaptor.security;

public class KWalletSecurityAdaptor extends SecretServiceSecurityAdaptor {

	public String getType() {
		return "kwallet";
	}

	@Override
	protected String getDefaultCollection() {
		return "passwords";
	}

}
