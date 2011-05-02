package fr.in2p3.jsaga.adaptor.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.U;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential;

public class UserPassStoreSecurityAdaptor implements SecurityAdaptor {

	private final String STOREFILE = "StoreFile";
	
	public String getType() {
		return "UserPassStore";
	}

	public Usage getUsage() {
    	return new UAnd(
   			 new Usage[]{
   					 new UFile(STOREFILE),
   			 }
   			 );
	}

	public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
       		 new Default(STOREFILE, new File[]{
                        new File(System.getProperty("user.home"), ".netrc")}),
        };
	}

	public Class getSecurityCredentialClass() {
		return UserPassStoreSecurityCredential.class;
	}

	public SecurityCredential createSecurityCredential(int usage,
			Map attributes, String contextId) throws IncorrectStateException,
			TimeoutException, NoSuccessException {
		UserPassStoreSecurityCredential upsc = new UserPassStoreSecurityCredential();
		BufferedReader br;
		String netrcFile = (String) attributes.get(STOREFILE);
		try {
			br = new BufferedReader(new FileReader(new File(netrcFile)));
		} catch (FileNotFoundException e) {
			throw new NoSuccessException("File not found: "+new File(netrcFile).toString());
		}
		String line;
		try {
			while ((line= br.readLine()) != null) {
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line);
				    String net_machine = null;
				    String net_user = null;
				    String net_password = null;
				    while (st.hasMoreTokens()) {
				        String token = st.nextToken();
				        if (token.equals("machine")) {
				            net_machine = st.nextToken();
				        } else if (token.equals("login")) {
				            net_user = st.nextToken();
				        } else if (token.equals("password"))
				            net_password = st.nextToken();
				    }
				    upsc.addUserPassCredential(net_machine, net_user, net_password);
				}
			}
		} catch (IOException e) {
			throw new NoSuccessException("Error reading file: "+new File(System.getProperty("user.home"),".netrc").toString());
		}
		return upsc;
	}

}
