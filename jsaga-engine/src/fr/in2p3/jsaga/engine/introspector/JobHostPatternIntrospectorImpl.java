package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.ServiceEngineConfigurationAbstract;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.error.*;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobHostPatternIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobHostPatternIntrospectorImpl extends AbstractIntrospectorImpl implements Introspector {
    private Execution m_config;
    private String m_host;

    public JobHostPatternIntrospectorImpl(Execution config, String host) throws NoSuccess {
        super(host);
        m_config = config;
        m_host = host;
    }

    protected String getChildIntrospectorType() {
        return Introspector.SERVICE;
    }

    /** @return services */
    protected String[] getChildIntrospectorKeys() throws NoSuccess {
        ServiceEngineConfigurationAbstract config = Configuration.getInstance().getConfigurations().getProtocolCfg();
        List<String> result = new ArrayList<String>();
        for (ServiceRef service : config.listServiceRefByHostname(m_config.getMapping(), m_host)) {
            result.add(service.getName());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Get child introspector by service
     * @param key the service
     * @return the created introspector
     */
    public Introspector getChildIntrospector(String key) throws NotImplemented, DoesNotExist, NoSuccess {
        for (JobService service : m_config.getJobService()) {
            if (service.getName().equals(key)) {
                return new JobServiceIntrospectorImpl(service);
            }
        }
        throw new DoesNotExist("Service not found: "+key);
    }
}
