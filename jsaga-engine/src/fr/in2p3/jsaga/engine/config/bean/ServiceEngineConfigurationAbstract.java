package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ServiceEngineConfigurationAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   15 févr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ServiceEngineConfigurationAbstract {
    public ServiceRef[] listServiceRefByHostname(Mapping mapping, String hostname) throws NoSuccess {
        if (mapping != null) {
            for (int d=0; d<mapping.getDomainCount(); d++) {
                Domain domain = mapping.getDomain(d);
                if (hostname.endsWith(getDomain(domain.getName()))) {
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
            return new ServiceRef[0];
        }
    }
    private static String getDomain(String domainName) {
        return (domainName!=null ? "."+domainName : "");
    }
}
