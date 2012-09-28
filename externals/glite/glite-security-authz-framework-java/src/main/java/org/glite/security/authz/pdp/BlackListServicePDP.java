package org.glite.security.authz.pdp;

import org.glite.security.authz.ServicePDP;
import org.glite.security.authz.ChainConfig;
import org.glite.security.authz.InitializeException;
import org.glite.security.authz.AuthorizationException;
import org.glite.security.authz.CloseException;
import org.glite.security.authz.InvalidPolicyException;
import org.glite.security.authz.AuthzUtil;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/**
 * BlackList ServicePDP implementation allowing blacklist files to be set.
 * The blacklist file may be updated at runtime.
 * @see ServicePDP
 */
public class BlackListServicePDP implements ServicePDP {
    private static Log logger =
        LogFactory.getLog(BlackListServicePDP.class.getName());

    /**
     * This configuration property should point to a file containing
     * a row separated list of Subject DNs that should be denied access.
     */
    public static final String BLACK_LIST_FILE = "blackListFile";
    private HashMap blackListMap = new HashMap();
    private long lastModified = 0;
    private File blackList;
    /**
     * initializes the interceptor with configuration information that are
     * valid up until the point when close is called.
     * @param config holding interceptor specific configuration values, that
     *               may be obtained using the name paramter
     * @param name the name that should be used to access all the interceptor
     *             local configuration
     * @param id the id in common for all interceptors in a chain (it is valid
     *          up until close is called)
     *          if close is not called the interceptor may assume that the id
     *          still exists after a process restart
     * @throws InitializeException if blacklist file was not found
     */
    public void initialize(ChainConfig config, String name, String id)
        throws InitializeException {
        String blackListFile =
            (String) config.getProperty(name, BLACK_LIST_FILE);
        if (blackListFile == null) {
            throw new InitializeException("Blacklist file not set");
        }
        this.blackList = new File(blackListFile);
        if (!this.blackList.exists()) {
            throw new InitializeException("Blacklist file not found "
                                          + blackListFile);
        }
        try {
            loadList();
        } catch (IOException io) {
            throw new InitializeException("Failed to load blacklist", io);
        }
    }
    private void loadList() throws IOException {
        if (this.blackList.lastModified() == this.lastModified) {
            return;
        }
        this.lastModified = this.blackList.lastModified();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(this.blackList));
            for (String user = reader.readLine();
                 user != null;
                 user = reader.readLine()) {
                this.blackListMap.put(user, user);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    /**
     * this operation is called by the PDP Framework whenever the application
     * needs to call secured operations. The PDP should return true if the
     * local policy allows the subject to invoke the operation. If the PDP
     * has no local knowledge about whether the operation is allowed or not
     * it should return false to allow other PDPs and PIPs in the chain to
     * continue the evaluation. Obligations to be read by other PIPs or PDPs
     * may be set as attributes in the Subject credentials.
     * @param peerSubject authenticated client subject with credentials
     *                    and attributes
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @return false if user was not found in the blacklist
     * @throws AuthorizationException if user was found in the blacklist
     */
    public boolean isPermitted(Subject peerSubject,
                               MessageContext context,
                               QName operation) throws AuthorizationException {
        String dn = AuthzUtil.getIdentity(peerSubject);
        logger.debug("Checking dn: " + dn);
        try {
            loadList();
        } catch (IOException io) {
            throw new AuthorizationException("Failed to load blacklist", io);
        }
        if (this.blackList != null) {
            if (blackListMap.get(dn) != null) {
                throw new AuthorizationException(dn + " black listed");
            }
        }
        return false;
    }
    /**
     * this method is called by the PDP framework to indicate that the
     * interceptor now should remove all state that was allocated in the
     * initialize call.
     * @throws CloseException if exception occured when closing this PDP
     */
    public void close() throws CloseException {
    }
}

