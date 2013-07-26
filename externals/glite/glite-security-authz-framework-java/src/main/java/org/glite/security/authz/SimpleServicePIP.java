package org.glite.security.authz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import java.util.Map;

/**
 * Simple ServicePIP implementation maintaining Subject DN to role mappings.
 * @see ServicePIP
 */
public class SimpleServicePIP implements ServicePIP {
     private static Log logger =
         LogFactory.getLog(SimpleServicePIP.class.getName());
     /**
     * This configuration property should point to a user (Subject DN)
     * keyed Map of arrays of Strings representing roles.
     */
    public static final String ROLE_MAPPINGS = "roleMappings";
    /**
     * The name of the attribute used to set the matching roles.
     */
    public static final String ROLES =
        "http://www.glite.org/security/authz/roles";
    private Map roleMappings;
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
     * @throws InitializeException if role mappings were not set
     */
    public void initialize(ChainConfig config, String name, String id)
        throws InitializeException {
        this.roleMappings = (Map) config.getProperty(name, ROLE_MAPPINGS);
        if (this.roleMappings == null) {
            throw new InitializeException("Role mappings not set");
        }
    }
    /**
     * collects attributes and populates the subject with
     * public or private credentials to be checked by subsequent
     * PDPs in the same interceptor chain.
     * @param peerSubject authenticated subject for which attributes
     *                    should be collected
     * @param context holds properties of this XML message exchange
     * @param operation operation that the subject wants to invoke
     * @throws AttributeException if an exception occurred while getting
     *                            the attributes
     */
    public void collectAttributes(Subject peerSubject,
                                 MessageContext context,
                                 QName operation) throws AttributeException {
        String id = AuthzUtil.getIdentity(peerSubject);
        logger.debug("collecting attributes for: " + id);
        String[] roles = (String[]) this.roleMappings.get(id);
        if (roles != null) {
            logger.debug("Got attributes for: " + id);
            PIPAttribute roleAttribute =
                new PIPAttribute(ROLES, roles);
            roleAttribute.addPublic(peerSubject);
        }
    }
     /**
     * this method is called by the PDP framework to indicate that the
     * interceptor now should remove all state that was allocated in the
     * initialize call.
     * @throws CloseException if exception occured when closing this PIP
     */
    public void close() throws CloseException {
    }
}

