package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.engine.descriptors.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;
import org.ogf.saga.url.URL;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ServiceAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ServiceAdaptorFactory {
    public static Map getAttributes(URL url, ContextImpl context, Map adaptorDefaults, String serviceType) throws NotImplementedException, BadParameterException, NoSuccessException {
        // map rather than properties, because map supports "put(key, null)"
        Map attributes = new HashMap();

        // add service config
        String scheme = context.getSchemeFromAlias(url.getScheme());
        Properties serviceConfig = context.getServiceConfig(serviceType, scheme);
        if (serviceConfig != null) {
            attributes.putAll(serviceConfig);
        } else {
            attributes.putAll(adaptorDefaults);
        }

        // add service call
        String query = url.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (int i=0; pairs!=null && i<pairs.length; i++) {
                String[] pair = pairs[i].split("=");
                switch (pair.length) {
                    case 1:
                        attributes.put(pair[0], null);
                        break;
                    case 2:
                        attributes.put(pair[0], pair[1]);
                        break;
                    default:
                        throw new BadParameterException("Bad query in URL: "+url);
                }
            }
        }
        return attributes;
    }

    protected static SecurityCredential getCredential(URL url, ContextImpl context, ClientAdaptor adaptor) throws NotImplementedException, AuthenticationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        if (context != null) {
            if (SecurityAdaptorDescriptor.isSupported(context.getCredentialClass(), adaptor.getSupportedSecurityCredentialClasses())) {
                try {
                    return context.getCredential();
                } catch (IncorrectStateException e) {
                    try {
                        throw new NoSuccessException("Invalid security context: "+context.getAttribute(Context.TYPE), e);
                    } catch (SagaException e2) {
                        throw new NoSuccessException("Invalid security context: "+e2.getMessage(), e);
                    }
                }
            } else if (SecurityAdaptorDescriptor.isSupportedNoContext(adaptor.getSupportedSecurityCredentialClasses())) {
                return null;
            } else {
                throw new AuthenticationFailedException("Security context class '"+context.getCredentialClass().getName()+"' not supported for protocol: "+url.getScheme());
            }
        } else if (SecurityAdaptorDescriptor.isSupportedNoContext(adaptor.getSupportedSecurityCredentialClasses())) {
            return null;
        } else {
            throw new AuthenticationFailedException("No security context configured for URL: "+url);
        }
    }
}
