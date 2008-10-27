package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.error.*;

import java.util.HashSet;
import java.util.Set;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSSchemeIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSSchemeIntrospectorImpl extends AbstractIntrospectorImpl implements Introspector {
    private Protocol m_config;

    public NSSchemeIntrospectorImpl(Protocol config) throws NoSuccessException {
        super(config.getScheme());
        m_config = config;
    }

    protected String getChildIntrospectorType() {
        return Introspector.HOST_PATTERN;
    }

    /** @return host patterns */
    protected String[] getChildIntrospectorKeys() {
        Set<String> result = new HashSet<String>();
        for (Domain domain : m_config.getMapping().getDomain()) {
            for (Host host : domain.getHost()) {
                if (host.getServiceRefCount() > 0) {
                    result.add(host.getPrefix()+"*"+getDomain(domain.getName()));
                }
            }
            if (domain.getServiceRefCount() > 0) {
                result.add("*"+getDomain(domain.getName()));
            }
        }
        if (m_config.getMapping().getServiceRefCount() > 0) {
            result.add("*.*");
        }
        return result.toArray(new String[result.size()]);
    }
    private static String getDomain(String domainName) {
        return (domainName!=null ? "."+domainName : "");
    }

    /**
     * Get child introspector by host
     * @param key the host
     * @return the created introspector
     */
    public Introspector getChildIntrospector(String key) throws NotImplementedException, DoesNotExistException, NoSuccessException {
        return new NSHostPatternIntrospectorImpl(m_config, key);
    }
}
