package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential;
//import fr.in2p3.jsaga.adaptor.security.impl.UserPassStoreSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.NoneSecurityCredential;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ogf.saga.context.Context;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;

/******************************************************
 * File:   BatchSSHAdaptorAbstract
 * Author: Taha BENYEZZA & Yassine BACHAR
 * Author: Lionel Schwarz
 * Date:   10 December 2010
 ****************************************************/
public abstract class BatchSSHAdaptorAbstract implements ClientAdaptor {

    public String getType() {
        return "pbs-ssh";
    }

    public int getDefaultPort() {
        return 22;
    }

    protected static final String KNOWN_HOSTS = "KnownHosts";
    
    // used if no working directory is defined in the job description
    // to write stdout and stderr
    // set as #PBS -d <dir>
    // If not defined, PBS will use $PBS_O_HOME which is $HOME with SSH
    protected static final String STAGING_DIRECTORY = "stagingDir";
    
    //public static final String USER_PUBLICKEY = "UserPublicKey";
    protected static KnownHosts KnownHosts = new KnownHosts();
    protected Connection connexion;
    private SecurityCredential credential;
    protected String m_stagingDirectory = null;
    
    public Usage getUsage() {
        return new UAnd(
                new Usage[]{
                    new UOptional(KNOWN_HOSTS),
                    new UOptional(STAGING_DIRECTORY),
                });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                    new Default(KNOWN_HOSTS, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/known_hosts")}),
                    /*new Default(Context.USERKEY, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/id_rsa"),
                        new File(System.getProperty("user.home") + "/.ssh/id_dsa")}),*/
                    /*new Default(USER_PUBLICKEY, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"),
                        new File(System.getProperty("user.home") + "/.ssh/id_dsa.pub")}),*/
                    new Default(Context.USERID,
                    System.getProperty("user.name"))
                };
    }
    
    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class, UserPassStoreSecurityCredential.class, SSHSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        this.credential = credential;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {

        try {
        	if (attributes.containsKey(STAGING_DIRECTORY)) 
        		this.m_stagingDirectory = (String) attributes.get(STAGING_DIRECTORY);
            // Creating a connection instance
            connexion = new Connection(host);
            // Now connect
            connexion.connect();

            // Load known_hosts file into in-memory KnownHosts
            if (attributes.containsKey(KNOWN_HOSTS)) {
                File knownHosts = new File((String) attributes.get(KNOWN_HOSTS));

                if (!knownHosts.exists()) {
                    throw new BadParameterException("Unable to find the selected known host file.");
                }

                KnownHosts.addHostkeys(knownHosts);
            }

            //connecting using a userId and a password
            if (credential instanceof UserPassSecurityCredential) {
                String userId = ((UserPassSecurityCredential) credential).getUserID();
                String password = ((UserPassSecurityCredential) credential).getUserPass();
                boolean isAuthenticated = connexion.authenticateWithPassword(userId, password);

                if (isAuthenticated == false) {
                    throw new AuthenticationFailedException("Authentication failed.");
                }
            } else if (credential instanceof UserPassStoreSecurityCredential) {
				try {
	        		String userId = ((UserPassStoreSecurityCredential) credential).getUserID(host);
	        		String password = ((UserPassStoreSecurityCredential) credential).getUserPass(host);
	                boolean isAuthenticated = connexion.authenticateWithPassword(userId, password);

	                if (isAuthenticated == false) {
	                    throw new AuthenticationFailedException("Authentication failed.");
	                }
				} catch (Exception e) {
	        		throw new AuthenticationFailedException(e);
				}
            } //connecting using private and public keys
        	else if(credential instanceof SSHSecurityCredential) {
        		String userId = ((SSHSecurityCredential) credential).getUserID();
        		String passPhrase = ((SSHSecurityCredential) credential).getUserPass();
        		// clone private key because the object will be reset
        		//byte[] privateKey = ((SSHSecurityCredential) credential).getPrivateKey().clone();
        		//byte[] publicKey = ((SSHSecurityCredential) credential).getPublicKey();
        		File Key = ((SSHSecurityCredential) credential).getPrivateKeyFile();

                if (!connexion.authenticateWithPublicKey(userId, Key, passPhrase)) {
                    throw new AuthenticationFailedException("Authentication failed.");
                }
            // TODO: The following block is useless
        	} else if (attributes.containsKey(Context.USERKEY)) {
                //getting the private key file
                File Key = new File((String) attributes.get(Context.USERKEY));

                if (!Key.exists()) {
                    throw new BadParameterException("Unable to find the selected known host file.");
                }

                boolean isAuthenticated = connexion.authenticateWithPublicKey((String) attributes.get(Context.USERID), Key, (String) attributes.get(Context.USERPASS));

                if (isAuthenticated == false) {
                    throw new AuthenticationFailedException("Authentication failed.");
                }
            } else {
                throw new AuthenticationFailedException("Invalid security instance.");
            }

        } catch (IOException ex) {
            System.out.println("Authentification error :"+ex.getMessage());
        }

    }

    public void disconnect() throws NoSuccessException {
        // Closing the connection
        connexion.close();
    }
    
    protected Session sendCommand(String command) throws IOException, BatchSSHCommandFailedException {
        Session session = connexion.openSession();

        session.execCommand(command);
        
        // waiting for the qsub command to end
        int conditions = session.waitForCondition( ChannelCondition.EXIT_STATUS, 0);

        int exitStatus = session.getExitStatus();
        if (exitStatus != 0) {
        	// try to read stderr
        	try {
	            BufferedReader br;
	            br = new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStderr())));
	            
	            String msg = br.readLine().split("MSG=")[1];
	        	
	        	throw new BatchSSHCommandFailedException(command , exitStatus, msg);
        	} catch (Exception e) {
	        	throw new BatchSSHCommandFailedException(command , exitStatus, "Unable to get error message");
        	}
        }
        return session;
    }
    
    protected List<BatchSSHJob> getAttributes(String nativeJobIdArray[]) throws NoSuccessException {
    	return this.getAttributes(nativeJobIdArray, null);
    }
    
    protected List<BatchSSHJob> getAttributes(String nativeJobIdArray[], String[] keys) throws NoSuccessException {
        //BatchSSHJob[] bj = new BatchSSHJob[nativeJobIdArray.length];
		List<BatchSSHJob> bj = new ArrayList<BatchSSHJob>();

        Session session = null;
    	String command = "qstat -f -1 ";
    	for (String jobId: nativeJobIdArray) {
    		command += jobId + " ";
    	}
        InputStream stdout;
        BufferedReader br;
        BatchSSHJob job = null;
        int i=0;
        try {
        	session = this.sendCommand(command);
            stdout = new StreamGobbler(session.getStdout());
            br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
            	line = line.trim();
            	if (line.startsWith("Job Id:")) {
                	String jobid = line.split(":")[1].trim();
                	job = new BatchSSHJob(jobid);
            	} else if (line.length() == 0) { // end of Job
            		//bj[i] = job;
            		bj.add(job);
            		i++;
            	} else { // attributes
            		String[] arr = line.split("=",2);
            		if (arr.length == 2) {
            			job.setAttribute(arr[0].trim().toUpperCase(), arr[1].trim());
            		}
            	}
            }
            br.close();

        } catch (IOException ex) {
			throw new NoSuccessException("Unable to query job status", ex);
        } catch (BatchSSHCommandFailedException e) {
			throw new NoSuccessException("Unable to query job status", e);
		} finally {
            if (session != null) session.close();
        }
		return bj;
		//return (BatchSSHJob[])bj.toArray(new BatchSSHJob[bj.size()]);

    }

}
