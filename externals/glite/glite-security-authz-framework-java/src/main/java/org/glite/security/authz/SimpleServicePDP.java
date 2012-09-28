package org.glite.security.authz;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

/**
 * Simple ServicePDP implementation allowing role permissions and
 * blacklists to be set.
 * @see ServicePDP
 */
public class SimpleServicePDP implements ServicePDP, ServicePAP {
    private static Log logger =
        LogFactory.getLog(SimpleServicePDP.class.getName());

    /**
     * This configuration property should point to a Map of blacklisted
     * Subject DNs.
     */
    public static final String BLACK_LIST = "blackList";
    /**
     * This configuration property should point to an operation (QName) keyed
     * Map of Maps with allowed users (Subject DNs).
     */
    public static final String ROLE_PERMISSION = "rolePermission";
    private static final String[] POLICIES =
                                new String[] {BLACK_LIST, ROLE_PERMISSION};
    private Map blackList;
    private Map allowedRoleOperations;
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
     * @throws InitializeException if role permission map was not set
     */
    public void initialize(ChainConfig config, String name, String id)
        throws InitializeException {
        this.blackList = (Map) config.getProperty(name, BLACK_LIST);
        this.allowedRoleOperations = (Map) config.getProperty(name,
                                                              ROLE_PERMISSION);
        if (this.allowedRoleOperations == null) {
            throw new InitializeException("Role permission map not set");
        }
    }
    /**
     * gets the names (typically uris) of all the policies that
     * the PDP supports.
     * @return array of policy names
     */
    public String[] getPolicyNames() {
        return POLICIES;
    }
    /**
     * gets the current policy of the PDP.
     * @param query may be used to query for a subset of a policy
     * @return the policy
     * @throws InvalidPolicyException if an invalid policy was detected
     */
    public Collection getPolicy(Node query) throws InvalidPolicyException {
        return null;
    }
    /**
     * sets the current policy of the PDP.
     * @param policy new policy
     * @return optional set policy result
     * @throws InvalidPolicyException if an invalid policy was passed in
     */
    public Collection setPolicy(Node policy) throws InvalidPolicyException {
        return null;
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
     * @return true if user role was found and permitted, otherwise false
     * @throws AuthorizationException if user was found on blacklist
     */
    public boolean isPermitted(Subject peerSubject,
                               MessageContext context,
                               QName operation) throws AuthorizationException {
        String dn = AuthzUtil.getIdentity(peerSubject);
        logger.debug("Checking dn: " + dn);
        if (this.blackList != null) {
            if (blackList.get(dn) != null) {
                throw new AuthorizationException(dn + " black listed");
            }
        }
        logger.debug("Getting map for operation " + operation);
        Map roleMap = (Map) this.allowedRoleOperations.get(operation);
        if (roleMap == null) {
            return false;
        }
        logger.debug("Got map for operation " + operation);
        boolean roleAllowed = false;
        Set credentials = peerSubject.getPublicCredentials(PIPAttribute.class);
        for (Iterator iterator = credentials.iterator();
             iterator.hasNext();) {
            PIPAttribute pipAttribute = (PIPAttribute) iterator.next();
            logger.debug("Found pip attribute " + pipAttribute.getName());
            if (pipAttribute.getName().equals(SimpleServicePIP.ROLES)) {
                String[] roles = (String[]) pipAttribute.getValue();
                for (int i = 0; i < roles.length; i++) {
                    logger.debug("Checking role: " + roles[i]
                                 + " against role map");
                    if (roleMap.get(roles[i]) != null) {
                        logger.debug("Found role: " + roles[i]);
                        roleAllowed = true;
                        break;
                    }
                }
            }
        }
        return roleAllowed;
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

