package fr.in2p3.jsaga.adaptor.batchssh.job;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.KnownHosts;
import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UOptional;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.NoneSecurityCredential;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
 * Date:   10 December 2010
 ****************************************************/
public abstract class BatchSSHAdaptorAbstract implements ClientAdaptor {

    protected static final String COMPRESSION_LEVEL = "CompressionLevel";
    protected static final String KNOWN_HOSTS = "KnownHosts";
    public static final String USER_PUBLICKEY = "UserPublicKey";
    protected static KnownHosts KnownHosts = new KnownHosts();
    protected Connection connexion;
    protected static Map sessionMap = new HashMap();
    private SecurityCredential credential;

    public Usage getUsage() {
        return new UAnd(
                new Usage[]{
                    new UOptional(KNOWN_HOSTS),
                    new UOptional(COMPRESSION_LEVEL)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
                    new Default(KNOWN_HOSTS, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/known_hosts")}),
                    new Default(Context.USERKEY, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/id_rsa"),
                        new File(System.getProperty("user.home") + "/.ssh/id_dsa")}),
                    new Default(USER_PUBLICKEY, new File[]{
                        new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"),
                        new File(System.getProperty("user.home") + "/.ssh/id_dsa.pub")}),
                    new Default(Context.USERID,
                    System.getProperty("user.name"))
                };
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class, NoneSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        this.credential = credential;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {

        try {

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
            } //connecting using private and public keys
            else if (attributes.containsKey(Context.USERKEY)) {
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
}
