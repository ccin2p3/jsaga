package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.NoSuccessException;

import java.util.HashMap;
import java.util.Map;

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
    public ServiceRef[] listServiceRefByHostname(Mapping mapping, String hostname) throws NoSuccessException {
        if (mapping != null) {
            if (hostname != null) {
                for (int d=0; d<mapping.getDomainCount(); d++) {
                    Domain domain = mapping.getDomain(d);
                    if ((domain.getName()!=null && hostname.endsWith("."+domain.getName())) ||
                        domain.getName()==null)
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
                if (mapping.getServiceRefCount() > 0) {
                    // todo: find a way to solve ambiguity
                    return new ServiceRef[]{mapping.getServiceRef(0)};
                } else if (mapping.getDomainCount() > 0) {
                    Map<String,ServiceRef> map = new HashMap<String,ServiceRef>();
                    for (int d=0; d<mapping.getDomainCount(); d++) {
                        Domain domain = mapping.getDomain(d);
                        for (int h=0; h<domain.getHostCount(); h++) {
                            Host host = domain.getHost(h);
                            addAll(map, host.getServiceRef());
                        }
                        addAll(map, domain.getServiceRef());
                    }
                    //addAll(map, mapping.getServiceRef());
                    return map.values().toArray(new ServiceRef[map.size()]);
                } else {
                    return new ServiceRef[0];
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
    private static void addAll(Map<String,ServiceRef> map, ServiceRef[] array) {
        for (ServiceRef serviceRef : array) {
            map.put(serviceRef.getName(), serviceRef);
        }
    }
}
