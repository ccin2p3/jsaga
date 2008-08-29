package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Execution;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.error.*;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobIntrospectorImpl extends AbstractIntrospectorImpl implements Introspector {
    private JobserviceEngineConfiguration m_config;

    public JobIntrospectorImpl() throws NoSuccess {
        super(System.getProperty("saga.factory"));
        m_config = Configuration.getInstance().getConfigurations().getJobserviceCfg();
    }

    protected String getChildIntrospectorType() {
        return Introspector.SCHEME;
    }

    /** @return schemes */
    protected String[] getChildIntrospectorKeys() throws NoSuccess {
        List<String> result = new ArrayList<String>();
        for (Execution execution : m_config.toXMLArray()) {
            result.add(execution.getScheme());
            for (String schemeAlias : execution.getSchemeAlias()) {
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
    public Introspector getChildIntrospector(String key) throws NotImplemented, DoesNotExist, NoSuccess {
        return new JobSchemeIntrospectorImpl(this.findExecution(key));
    }
    private Execution findExecution(String scheme) throws DoesNotExist, ConfigurationException {
        for (Execution execution : m_config.toXMLArray()) {
            if (execution.getScheme().equals(scheme)) {
                return execution;
            } else {
                for (String schemeAlias : execution.getSchemeAlias()) {
                    if (schemeAlias.equals(scheme)) {
                        return execution;
                    }
                }
            }
        }
        throw new DoesNotExist("Scheme not found: "+scheme);
    }
}
