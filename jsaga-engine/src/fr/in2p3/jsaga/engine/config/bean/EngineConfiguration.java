package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.schema.config.EffectiveConfig;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.*;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EngineConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EngineConfiguration {
    private ContextEngineConfiguration m_contextCfg;
    private ProtocolEngineConfiguration m_protocolCfg;
    private JobserviceEngineConfiguration m_jobserviceCfg;
    private EffectiveConfig m_xml;

    public EngineConfiguration(EffectiveConfig mergedConfig) {
        m_contextCfg = new ContextEngineConfiguration(mergedConfig.getContext());
        m_protocolCfg = new ProtocolEngineConfiguration(mergedConfig.getProtocol());
        m_jobserviceCfg = new JobserviceEngineConfiguration(mergedConfig.getExecution());
        m_xml = mergedConfig;
    }

    public ContextEngineConfiguration getContextCfg() {
        return m_contextCfg;
    }

    public ProtocolEngineConfiguration getProtocolCfg() {
        return m_protocolCfg;
    }

    public JobserviceEngineConfiguration getJobserviceCfg() {
        return m_jobserviceCfg;
    }

    public void dump(OutputStream out) throws ValidationException, MarshalException {
        LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
        Writer writer = new OutputStreamWriter(out);
        Marshaller.marshal(m_xml, writer);
    }
}
