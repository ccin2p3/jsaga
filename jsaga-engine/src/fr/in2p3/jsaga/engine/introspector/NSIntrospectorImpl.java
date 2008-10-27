package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.error.*;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSIntrospectorImpl extends AbstractIntrospectorImpl implements Introspector {
    private ProtocolEngineConfiguration m_config;

    public NSIntrospectorImpl() throws NoSuccessException {
        super(System.getProperty("saga.factory"));
        m_config = Configuration.getInstance().getConfigurations().getProtocolCfg();
    }

    protected String getChildIntrospectorType() {
        return Introspector.SCHEME;
    }

    /** @return schemes */
    protected String[] getChildIntrospectorKeys() throws NoSuccessException {
        List<String> result = new ArrayList<String>();
        for (Protocol protocol : m_config.toXMLArray()) {
            result.add(protocol.getScheme());
            for (String schemeAlias : protocol.getSchemeAlias()) {
                result.add(schemeAlias);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Get child introspector by scheme
     * @param key the scheme
     * @return the created introspector
     */
    public Introspector getChildIntrospector(String key) throws NotImplementedException, DoesNotExistException, NoSuccessException {
        return new NSSchemeIntrospectorImpl(this.findProtocol(key));
    }
    private Protocol findProtocol(String scheme) throws DoesNotExistException, ConfigurationException {
        for (Protocol protocol : m_config.toXMLArray()) {
            if (protocol.getScheme().equals(scheme)) {
                return protocol;
            } else {
                for (String schemeAlias : protocol.getSchemeAlias()) {
                    if (schemeAlias.equals(scheme)) {
                        return protocol;
                    }
                }
            }
        }
        throw new DoesNotExistException("Scheme not found: "+scheme);
    }
}
