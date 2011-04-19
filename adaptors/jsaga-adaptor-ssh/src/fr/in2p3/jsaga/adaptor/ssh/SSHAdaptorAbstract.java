package fr.in2p3.jsaga.adaptor.ssh;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.local.LocalJobProcess;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import fr.in2p3.jsaga.adaptor.ssh.job.SSHJobProcess;
import fr.in2p3.jsaga.adaptor.ssh.security.SSHSecurityCredential;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityFile;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

/**
 * TODO : Test of compression
 */

public abstract class SSHAdaptorAbstract implements ClientAdaptor {
	
	protected static final String COMPRESSION_LEVEL = "CompressionLevel";
	protected static final String KNOWN_HOSTS = "KnownHosts";
	protected Session session;
	protected static Map sessionMap = new HashMap();
	private SecurityCredential credential;
	private int compression_level = 0;

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class, SSHSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
    	this.credential = credential;
    } 
	
    public int getDefaultPort() {
        return 22;
    }
    
    public Usage getUsage() {
    	return new UAnd(
      			 new Usage[]{
      					 new UOptional(KNOWN_HOSTS),
      					 new UOptional(COMPRESSION_LEVEL)});
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
    	return new Default[]{
                new Default(KNOWN_HOSTS, new File[]{
                		new File(System.getProperty("user.home")+"/.ssh/known_hosts")})
           };
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
    		
    	try {
    		
    		JSch jsch = new JSch();
    		// set known hosts file : checking will be done
    		if (attributes.containsKey(KNOWN_HOSTS)) {
    			if(!new File((String) attributes.get(KNOWN_HOSTS)).exists())
    				throw new BadParameterException("Unable to find the selected known host file.");
				jsch.setKnownHosts((String) attributes.get(KNOWN_HOSTS));
			}
    		
    		if(credential instanceof UserPassSecurityCredential) {
        		String userId = ((UserPassSecurityCredential) credential).getUserID();
        		String password = ((UserPassSecurityCredential) credential).getUserPass();
        		session = jsch.getSession(userId, host, port);
        		session.setPassword(password);
        	}
        	else if(credential instanceof SSHSecurityCredential) {
        		String userId = ((SSHSecurityCredential) credential).getUserID();
        		String passPhrase = ((SSHSecurityCredential) credential).getUserPass();
        		// clone private key because the object will be reset
        		byte[] privateKey = ((SSHSecurityCredential) credential).getPrivateKey().clone();
        		byte[] publicKey = ((SSHSecurityCredential) credential).getPublicKey();

        		// create identity
        		Identity identity = IdentityFile.newInstance(userId, privateKey, publicKey, jsch);
        		if(passPhrase != null)
        			jsch.addIdentity(identity, passPhrase.getBytes());
        		else
        			jsch.addIdentity(identity, null);
    			session = jsch.getSession(userId, host, port);
        	}
        	else {
        		throw new AuthenticationFailedException("Invalid security instance.");
        	}
    		
    		// checking know host will not be done
    		if (!attributes.containsKey(KNOWN_HOSTS)) {
    			session.setConfig("StrictHostKeyChecking", "no");
    		}
    		
    		// checking compression level
    		if (attributes!=null && attributes.containsKey(COMPRESSION_LEVEL)) {
    			try {
    				compression_level = Integer.valueOf(((String) attributes.get(COMPRESSION_LEVEL)));
    				if(compression_level > 9 || compression_level < 0 )
    					throw new BadParameterException("Invalid value for CompressionLevel attribute: "+ compression_level+ " must be in the 0-9 range.");
    			}
    			catch (NumberFormatException e) {
    				throw new BadParameterException("Unable to parse CompressionLevel attribute.",e);
    			}
    		}
			if (compression_level == 0) {
				session.setConfig("compression.s2c", "none");
				session.setConfig("compression.c2s", "none");
			} else {
				session.setConfig("compression_level", String.valueOf(compression_level));
				session.setConfig("compression.s2c","zlib@openssh.com,zlib,none");
				session.setConfig("compression.c2s","zlib@openssh.com,zlib,none");
			}
			// oonnect
    		session.connect();
    	} catch (JSchException e) {
    		if(e.getMessage().equals("Auth fail"))
    			throw new AuthenticationFailedException(e);
    		throw new NoSuccessException("Unable to connect to server", e);
    	}
    }

    public void disconnect() throws NoSuccessException {
    	session.disconnect();
        session = null;
    }
    
    // TODO: store via SSH
    public static void store(SSHJobProcess p, String nativeJobId) throws IOException {
    	byte[] buf = serialize(p);
    	FileOutputStream f = new FileOutputStream(new File(SSHJobProcess.getRootDir() + "/" + nativeJobId + ".process"));
    	f.write(buf);
    	f.close();
    }
    
    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(obj);
        oos.close();
        return buffer.toByteArray();
    }

    // TODO: restore via SSH
    public static SSHJobProcess restore(String nativeJobId) throws IOException, ClassNotFoundException {
    	File f = new File(LocalJobProcess.getRootDir() + "/" + nativeJobId + ".process");
    	FileInputStream fis = new FileInputStream(f);
    	byte[] buf = new byte[(int)f.length()];
    	int len = fis.read(buf);
    	fis.close();
    	return (SSHJobProcess)deserialize(buf);
    }
    
    private static Object deserialize(byte[] bytes)
            throws ClassNotFoundException {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(input);
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("error reading from byte-array!");
        }
    }
    
}
