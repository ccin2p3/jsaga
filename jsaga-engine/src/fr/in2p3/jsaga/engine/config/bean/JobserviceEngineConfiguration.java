package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.URL;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobserviceEngineConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobserviceEngineConfiguration extends ServiceEngineConfigurationAbstract {
    private Execution[] m_execution;

    public JobserviceEngineConfiguration(Execution[] execution) {
        m_execution = execution;
    }

    public Execution[] toXMLArray() {
        return m_execution;
    }

    public JobService findJobService(URL url) throws NotImplemented, NoSuccess {
        if (url != null) {
            Execution execution = findExecution(url.getScheme());
            if (url.getFragment() != null) {
                String serviceRef = url.getFragment();
                JobService service = this.findJobServiceByServiceRef(execution, serviceRef);
                if (service != null) {
                    return service;
                } else if (execution.getJobService(0).getContextRef() == null) {
                    return execution.getJobService(0);
                } else {
                    String contextRef = url.getFragment();
                    service = this.findJobServiceByContextRef(execution, contextRef);
                    if (service != null) {
                        return service;
                    } else {
                        throw new NoSuccess("No job service matches fragment: "+url.getFragment());
                    }
                }
            } else {
                ServiceRef[] arrayRef = super.listServiceRefByHostname(execution.getMapping(), url.getHost());
                switch(arrayRef.length) {
                    case 0:
                        throw new NoSuccess("No job service matches hostname: "+url.getHost());
                    case 1:
                        String serviceRef = arrayRef[0].getName();
                        JobService service = this.findJobServiceByServiceRef(execution, serviceRef);
                        if (service != null) {
                            return service;
                        } else {
                            throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");
                        }
                    default:
                        throw new NoSuccess("Ambiguity: several job services match hostname: "+url.getHost());
                }
            }
        } else {
            throw new NoSuccess("URL is null");
        }
    }

    public Execution findExecution(String scheme) throws NoSuccess {
        for (int j=0; j<m_execution.length; j++) {
            Execution job = m_execution[j];
            if (job.getScheme().equals(scheme)) {
                return job;
            }
        }
        throw new NoSuccess("No execution manager matches scheme: "+ scheme);
    }

    private JobService findJobServiceByServiceRef(Execution execution, String serviceRef) throws NoSuccess {
        for (int s=0; s<execution.getJobServiceCount(); s++) {
            JobService service = execution.getJobService(s);
            if (service.getName().equals(serviceRef)) {
                return service;
            }
        }
        return null;
    }

    private JobService findJobServiceByContextRef(Execution execution, String contextRef) throws NoSuccess {
        for (int s=0; s<execution.getJobServiceCount(); s++) {
            JobService service = execution.getJobService(s);
            if (service.getContextRef().equals(contextRef)) {
                return service;
            }
        }
        return null;
    }
}
