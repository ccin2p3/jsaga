package org.glite.security.authz.providers;

import java.security.Provider;
import java.security.Security;

/**
 * Provider for implementation of ServiceAuthorizationChains.
 * @see org.glite.security.authz.ServiceAuthorizationChain
 */
public final class AuthzProvider extends Provider {
    /**
     * Singleton provider instance.
     */
    private static AuthzProvider provider = new AuthzProvider();
    /**
     * Constructor.
     */
    public AuthzProvider() {
        super("Glite",
              1.0,
              "Glite Authorization (ServiceAuthorizationChain) Provider");
        setProperty("ServiceAuthorizationChain.DENYOVERRIDES",
                    DenyOverridesChain.class.getName());
        setProperty("ServiceAuthorizationChain.PERMITOVERRIDES",
                    PermitOverridesChain.class.getName());
        setProperty("ServiceAuthorizationChain.FIRSTAPPLICABLE",
                    FirstApplicableChain.class.getName());
    }
    /**
     * gets an instance of a provider.
     * @return provider instance
     */
    public static AuthzProvider getInstance() {
        return provider;
    }
    /**
     * adds provider to global list of trusted security providers.
     */
    public static void addProvider() {
        Security.addProvider(getInstance());
    }
}

