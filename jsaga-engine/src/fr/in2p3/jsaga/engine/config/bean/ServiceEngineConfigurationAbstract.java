package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ServiceEngineConfigurationAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 f�vr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ServiceEngineConfigurationAbstract {
    public ServiceRef[] listServiceRefByHostname(String scheme, Mapping mapping, String hostname) throws NoSuccess {
        if (mapping != null) {
            if (hostname != null) {
                for (int d=0; d<mapping.getDomainCount(); d++) {
                    Domain domain = mapping.getDomain(d);
                    if ((domain.getName()!=null && hostname.endsWith("."+domain.getName())) ||
                        (domain.getName()==null && hostname.indexOf(".")==-1))
                    {
                        for (int h=0; h<domain.getHostCount(); h++) {
                            Host host = domain.getHost(h);
                            if (hostname.startsWith(host.getPrefix())) {
                                return host.getServiceRef();
                            }
                        }
                        return domain.getServiceRef();
                    }
                }
                return mapping.getServiceRef();
            } else {
                // if no hostname
                if (mapping.getDomainCount() > 1) {
                    throw new AmbiguityException(scheme, "several services match and no hostname is provided");
                } else {
                    return mapping.getServiceRef();
                }
            }
        } else {
            // if no mapping
            return new ServiceRef[0];
        }
    }
    private static String getDomain(String domainName) {
        return (domainName!=null ? "."+domainName : "");
    }
}
