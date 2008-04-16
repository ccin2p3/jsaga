package fr.in2p3.jsaga.adaptor.ssh;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.ssh.security.SSHSecurityAdaptor;

import org.ogf.saga.error.AuthenticationFailed;
import org.ogf.saga.error.AuthorizationFailed;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.error.Timeout;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   11 avril 2008
* ***************************************************/
public abstract class SSHAdaptorAbstract implements SagaSecureAdaptor {
	
	public static final String KNOWN_HOSTS = "KnownHosts";
	protected Session session;
	protected static Map sessionMap = new HashMap();
	private SecurityAdaptor securityAdaptor;
	
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class, SSHSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
    	this.securityAdaptor = securityAdaptor;
    } 
	
    public int getDefaultPort() {
        return 22;
    }
    
    public Usage getUsage() {
    	return new UAnd(
      			 new Usage[]{
      					 new UOptional(KNOWN_HOSTS)});
    }

    public Default[] getDefaults(Map map) throws IncorrectState {
    	return new Default[]{
                new Default(KNOWN_HOSTS, new File[]{
                		new File(System.getProperty("user.home")+"/.ssh/known_hosts")})
           };
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
    		
    	try {
    		
    		JSch jsch = new JSch();
    		// set known hosts file : checking will be done
    		if (attributes.containsKey(KNOWN_HOSTS)) {
    			if(!new File((String) attributes.get(KNOWN_HOSTS)).exists())
    				throw new BadParameter("Unable to find the selected known host file.");    			
				jsch.setKnownHosts((String) attributes.get(KNOWN_HOSTS));
			}
    		
    		if(securityAdaptor instanceof UserPassSecurityAdaptor) {
        		String userId = ((UserPassSecurityAdaptor) securityAdaptor).getUserID();
        		String password = ((UserPassSecurityAdaptor) securityAdaptor).getUserPass();
        		session = jsch.getSession(userId, host, port);
        		session.setPassword(password);
        	}
        	else if(securityAdaptor instanceof SSHSecurityAdaptor) {
        		String userId = ((SSHSecurityAdaptor) securityAdaptor).getUserID();
        		String passPhrase = ((SSHSecurityAdaptor) securityAdaptor).getUserPass();
        		// clone private key because the object will be reset
        		byte[] privateKey = ((SSHSecurityAdaptor) securityAdaptor).getPrivateKey().clone();
        		byte[] publicKey = ((SSHSecurityAdaptor) securityAdaptor).getPublicKey();

        		// create identity
        		Identity identity = IdentityFile.newInstance(userId, privateKey, publicKey, jsch);
        		if(passPhrase != null)
        			jsch.addIdentity(identity, passPhrase.getBytes());
        		else
        			jsch.addIdentity(identity, null);
    			session = jsch.getSession(userId, host, port);
        	}
        	else {
        		throw new AuthenticationFailed("Invalid security instance.");
        	}
    		
    		// checking know host will not be done
    		if (!attributes.containsKey(KNOWN_HOSTS)) {
    			session.setConfig("StrictHostKeyChecking", "no");
    		}
			session.connect();
    	} catch (JSchException e) {
    		if(e.getMessage().equals("Auth fail"))
    			throw new AuthenticationFailed(e);
    		throw new NoSuccess("Unable to connect to server", e);
    	}
    }

    public void disconnect() throws NoSuccess {
    	session.disconnect();
        session = null;
    } 
 }
