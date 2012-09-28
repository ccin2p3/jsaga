package org.glite.security.authz.pdp;

import org.glite.security.authz.ServicePDP;
import org.glite.security.authz.InitializeException;
import org.glite.security.authz.InvalidPolicyException;
import org.glite.security.authz.AuthorizationException;
import org.glite.security.authz.CloseException;
import org.glite.security.authz.ChainConfig;
import org.glite.security.authz.AuthzUtil;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.File;
import org.globus.gsi.gridmap.GridMap;

/**
 * Simple ServicePDP implementation allowing role permissions and
 * blacklists to be set.
 * @see ServicePDP
 */
public class GridMapServicePDP implements ServicePDP {
    private static Log logger =
        LogFactory.getLog(GridMapServicePDP.class.getName());
    /**
     * Property used to set in-memory grid map.
     */
    public static final String GRID_MAP = "gridMap";
    /**
     * Property used to set grid map file name.
     */
    public static final String GRID_MAP_FILE = "gridMapFile";
    private GridMap gridMap;
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
     * @throws InitializeException if gridmap was not found
     */
    public void initialize(ChainConfig config, String name, String id)
        throws InitializeException {
        this.gridMap = (GridMap) config.getProperty(name, GRID_MAP);
        if (this.gridMap == null) {
            String file = (String) config.getProperty(name, GRID_MAP_FILE);
            if (file == null) {
                throw new InitializeException("gridMapFileNotSpecified");
            }
            File gridMapFile = new File(file);
            if (!gridMapFile.exists()) {
                throw new InitializeException("gridMapFileNotFound");
            }
            this.gridMap = new GridMap();
            try {
                this.gridMap.load(gridMapFile);
            } catch (IOException ioe) {
                throw new InitializeException("gridMapFileLoadFailure "
                                              + file, ioe);
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
     * @return true if user was found in gridmap, otherwise false
     * @throws AuthorizationException if an exception occured during evaluation
     */
    public boolean isPermitted(Subject peerSubject,
                               MessageContext context,
                               QName operation) throws AuthorizationException {
        String dn = AuthzUtil.getIdentity(peerSubject);
        try {
            this.gridMap.refresh();
        } catch (IOException ioe) {
            throw new AuthorizationException("gridMapRefreshFailure", ioe);
        }
        String[] users = this.gridMap.getUserIDs(dn);
        if (users == null || users.length < 1) {
            return false;
        }
        LocalUserPIPAttribute userAttribute = new LocalUserPIPAttribute(users);
        userAttribute.addPublic(peerSubject);
        return true;
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

