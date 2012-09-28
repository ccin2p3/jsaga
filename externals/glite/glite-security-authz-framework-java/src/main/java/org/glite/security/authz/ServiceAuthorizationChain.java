package org.glite.security.authz;

// TODO internationalize according to gLite standards
// import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;

import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.util.Collection;

/**
 * The <code>ServiceAuthorizationChain</code> class ties together and evaluates
 * chains of {@link ServicePDP} and {@link ServicePIP} implementations.
 * The chain is evaluated in a strict configuration determined order. If any
 * PIP or PDP throws an Exception the evaluation is stopped, and the
 * Exception is propagated back to the client. If a PDP returns true the
 * evaluation is stopped and the operation is let through. If a PDP returns
 * false or a PIP returns normally the next interceptor in the chain is
 * evaluated.
 * Chains can also be linked, in which case the parent chain is typically
 * evaluated before the child chain.
 * The {@link ServiceAuthorizationChainSpi} interface should be implemented by
 * providers who want to implement their own policy combining algorithms. The
 * default algorithm is permit overrides.
 * @see ServiceInterceptor
 */
public final class ServiceAuthorizationChain implements ServiceInterceptor,
                                                        ServicePAP,
                                                        ServicePDP {
    private static Log logger =
        LogFactory.getLog(ServiceAuthorizationChain.class.getName());
    /**
     * Algorithm that stops evaluation if a deny result is detected
     * (default).
     */
    public static final String DENY_OVERRIDES_ALGORITHM = "DENYOVERRIDES";
    /**
     * Algorithm that stops evaluation if a permit result is detected.
     */
    public static final String PERMIT_OVERRIDES_ALGORITHM = "PERMITOVERRIDES";
    /**
     * Algorithm that stops evaluation if a permit or deny result is detected
     */
    public static final String FIRST_APPLICABLE_ALGORITHM = "FIRSTAPPLICABLE";

    private ServiceAuthorizationChainSpi engine;

    /**
     * gets an instance of a chain from a provider implementing the default
     * algorithm.
     * @return chain instance
     * @throws ClassNotFoundException if provider class was not found
     * @throws InstantiationException if provider could not be created
     * @throws IllegalAccessException if caller is not allowed to create
     *                                provider
     */
    public static ServiceAuthorizationChain getInstance()
            throws ClassNotFoundException,
                   InstantiationException,
                   IllegalAccessException {
        return getInstance(DENY_OVERRIDES_ALGORITHM);
    }
    /**
     * gets an instance of a chain from a provider implementing the specified
     * algorithm.
     * @param algorithm algorithm that the provider should implement
     * @return chain instance
     * @throws ClassNotFoundException if provider class was not found
     * @throws InstantiationException if provider could not be created
     * @throws IllegalAccessException if caller is not allowed to create
     *                                provider
     */
    public static ServiceAuthorizationChain getInstance(String algorithm)
            throws ClassNotFoundException,
                   InstantiationException,
                   IllegalAccessException {
        Provider[] providers = Security.getProviders();
        String spiClass = null;
        for (int i = 0; i < providers.length; i++) {
            spiClass = providers[i].getProperty("ServiceAuthorizationChain."
                                                + algorithm);
            if (spiClass != null) {
                break;
            }
        }
        if (spiClass == null) {
            throw new ProviderException("providerNotFound");
        }
        Class cls = ServiceAuthorizationChain.class.getClassLoader().
                       loadClass(spiClass);
        return new ServiceAuthorizationChain(
                             (ServiceAuthorizationChainSpi) cls.newInstance());
    }
    /**
     * gets an instance of a chain from the specified provider implementing the
     * default algorithm.
     * @param provider provider instance that should provide the algorithm
     * @return chain instance
     * @throws ClassNotFoundException if provider class was not found
     * @throws InstantiationException if provider could not be created
     * @throws IllegalAccessException if caller is not allowed to create
     *                                provider
     */
    public static ServiceAuthorizationChain getInstance(Provider provider)
            throws ClassNotFoundException,
                   InstantiationException,
                   IllegalAccessException {
        return getInstance(DENY_OVERRIDES_ALGORITHM, provider);
    }
    /**
     * gets an instance of a chain from the specified provider implementing the
     * specified algorithm.
     * @param algorithm algorithm that the provider should implement
     * @param provider provider instance that should provide the algorithm
     * @return chain instance
     * @throws ClassNotFoundException if provider class was not found
     * @throws InstantiationException if provider could not be created
     * @throws IllegalAccessException if caller is not allowed to create
     *                                provider
     */
    public static ServiceAuthorizationChain getInstance(String algorithm,
                                                        Provider provider)
            throws ClassNotFoundException,
                   InstantiationException,
                   IllegalAccessException {
        String spiClass = provider.getProperty("ServiceAuthorizationChain."
                                               + algorithm);
        if (spiClass == null) {
            throw new ProviderException("providerNotFound");
        }
        Class cls = ServiceAuthorizationChain.class.getClassLoader().
                            loadClass(spiClass);
        return new ServiceAuthorizationChain(
                             (ServiceAuthorizationChainSpi) cls.newInstance());
    }
    private ServiceAuthorizationChain(ServiceAuthorizationChainSpi spi) {
        this.engine = spi;
    }
    /**
     * sets the parent chain, which typically will be evaluated before the
     * current chain all authorization, get- and setPolicy, and getPolicyNames
     * requests are propagated to the parent, wheras initialize and close are
     * always only done on the local chain.
     * @param parentChain parent chain to connect to this chain.
     */
    public void setParent(ServiceAuthorizationChain parentChain) {
        this.engine.engineSetParent(parentChain);
    }
    /**
     * initializes the chain with a given configuration of PIPs and PDPs.
     * @param config configuration holding the names and classes of the
     *               ServicePDP and ServicePIP inteceptors
     * @param name name of this chain
     * @param id service id associated with this chain
     * @throws InitializeException if exception occured during initialization
     */
    public synchronized void initialize(ChainConfig config,
                                        String name,
                                        String id) throws InitializeException {
        this.engine.engineInitialize(config, name, id);
    }
    /**
     * gets the names of the policies implemented by this engine.
     * @return array of policy names
     */
    public String[] getPolicyNames() {
        return this.engine.engineGetPolicyNames();
    }
    /**
     * gets the policies of all the PDPs in this chain.
     * @param policy possible filter restricting the result of query
     * @return a Collection of Node objects returned by the PDPs
     * @throws InvalidPolicyException if an invalid policy filter was
     *                                specified
     */
    public Collection getPolicy(Node policy) throws InvalidPolicyException {
        return this.engine.engineGetPolicy(policy);
    }
    /**
     * sets the policies of all the PDPs in this chain.
     * @param policy the new policy or an update request
     * @return a Collection of Node values returned by the PDPs
     * @throws InvalidPolicyException if an invalid policy was specified
     */
    public Collection setPolicy(Node policy) throws InvalidPolicyException {
        return this.engine.engineSetPolicy(policy);
    }
    /**
     * should be called if an authorization decision must be made for an
     * authenticated subject.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    public boolean authorize(Subject peerSubject,
                             MessageContext context)
            throws AuthorizationException {
        return this.engine.engineAuthorize(peerSubject, context);
    }
    /**
     * should be called if an authorization decision must be made for an
     * authenticated subject.
     * @param peerSubject subject to be authorized
     * @param context JAX-RPC runtime context
     * @param operation operation requested to be performed
     * @return true if peerSubject was successfully authorized, false if no
     *         PDP in the chain could make a decision
     * @throws AuthorizationException if the authorization must be denied
     *                                without further processing
     */
    public boolean isPermitted(Subject peerSubject,
                             MessageContext context,
                             QName operation)
            throws AuthorizationException {
        return this.engine.engineIsPermitted(peerSubject, context, operation);
    }
    /**
     * should be called when the chain should be closed. That is, the
     * information sent in the init call is out of scope after this operation
     * has been invoked.
     * @throws CloseException if there was a problem closing this chain
     */
    public void close() throws CloseException {
        this.engine.engineClose();
    }
}

