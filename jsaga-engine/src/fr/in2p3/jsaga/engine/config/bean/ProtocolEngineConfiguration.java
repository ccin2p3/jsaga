package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.config.*;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.url.URL;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ProtocolEngineConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ProtocolEngineConfiguration extends ServiceEngineConfigurationAbstract {
    private Protocol[] m_protocol;

    public ProtocolEngineConfiguration(Protocol[] protocol) {
        m_protocol = protocol;
    }

    public Protocol[] toXMLArray() {
        return m_protocol;
    }

    public DataService findDataService(URL url, boolean isLogical) throws NotImplementedException, NoSuccessException {
        if (url != null) {
            Protocol protocol = findProtocol(url.getScheme(), isLogical);
            if (url.getFragment() != null) {
                String serviceRef = url.getFragment();
                DataService service = this.findDataServiceByServiceRef(protocol, serviceRef);
                if (service != null) {
                    return service;
                } else if (protocol.getDataService(0).getContextRef() == null) {
                    return protocol.getDataService(0);
                } else {
                    String contextRef = url.getFragment();
                    service = this.findDataServiceByContextRef(protocol, contextRef);
                    if (service != null) {
                        return service;
                    } else {
                        throw new NoMatchException(protocol.getScheme(), "no data service matches fragment "+url.getFragment());
                    }
                }
            } else {
                ServiceRef[] arrayRef = super.listServiceRefByHostname(protocol.getMapping(), url.getHost());
                switch(arrayRef.length) {
                    case 0:
                        throw new NoMatchException(protocol.getScheme(), "no data service matches hostname "+url.getHost());
                    case 1:
                        String serviceRef = arrayRef[0].getName();
                        DataService service = this.findDataServiceByServiceRef(protocol, serviceRef);
                        if (service != null) {
                            return service;
                        } else {
                            throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");
                        }
                    default:
                        throw new AmbiguityException(protocol.getScheme(), "several data services match hostname "+url.getHost());
                }
            }
        } else {
            throw new NoSuccessException("URL is null");
        }
    }

    public Protocol findProtocol(String scheme, boolean requiresLogical) throws NoSuccessException {
        List<Protocol> candidates = new ArrayList<Protocol>();
        for (int p=0; p<m_protocol.length; p++) {
            Protocol protocol = m_protocol[p];
            if (protocol.getScheme().equals(scheme) || StringArray.arrayContains(protocol.getSchemeAlias(), scheme)) {
                candidates.add(protocol);
            }
        }
        switch (candidates.size()) {
            case 0:
                throw new NoSuccessException("Protocol not found: "+scheme);
            case 1:
                return candidates.get(0);
            default:
                for (int p=0; p<candidates.size(); p++) {
                    Protocol protocol = candidates.get(p);
                    boolean isLogical = protocol.hasLogical() && protocol.getLogical();
                    if (isLogical && requiresLogical) {
                        return protocol;
                    } else if (!isLogical && !requiresLogical) {
                        return protocol;
                    }
                }
                if (requiresLogical) {
                    throw new NoSuccessException("No logical protocol found for scheme: "+scheme);
                } else {
                    throw new NoSuccessException("No physical protocol found for scheme: "+scheme);
                }
        }
    }

    private DataService findDataServiceByServiceRef(Protocol protocol, String serviceRef) {
        for (int s=0; s<protocol.getDataServiceCount(); s++) {
            DataService service = protocol.getDataService(s);
            if (service.getName().equals(serviceRef)) {
                return service;
            }
        }
        return null;
    }

    private DataService findDataServiceByContextRef(Protocol protocol, String contextRef) {
        for (int s=0; s<protocol.getDataServiceCount(); s++) {
            DataService service = protocol.getDataService(s);
            if (service.getContextRef().equals(contextRef)) {
                return service;
            }
        }
        return null;
    }
}
