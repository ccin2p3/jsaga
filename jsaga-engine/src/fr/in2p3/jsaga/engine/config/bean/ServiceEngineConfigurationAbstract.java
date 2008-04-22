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
            if (hostname==null && mapping.getDomainCount()>0) {
                throw new NoSuccess("Host name is required for this protocol");
            }
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
            return new ServiceRef[0];
        }
    }
    private static String getDomain(String domainName) {
        return (domainName!=null ? "."+domainName : "");
    }
}
