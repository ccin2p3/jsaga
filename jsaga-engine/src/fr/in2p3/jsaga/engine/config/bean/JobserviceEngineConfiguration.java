package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.ConfAttributesMap;
import fr.in2p3.jsaga.engine.config.UserAttributesMap;
import fr.in2p3.jsaga.engine.config.adaptor.JobAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.Attribute;
import fr.in2p3.jsaga.engine.schema.config.Jobservice;
import org.ogf.saga.error.NoSuccess;

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
public class JobserviceEngineConfiguration {
    private Jobservice[] m_jobservice;

    public JobserviceEngineConfiguration(Jobservice[] jobservice, JobAdaptorDescriptor desc, UserAttributesMap userAttributes) throws Exception {
        m_jobservice = jobservice;
        for (int i=0; m_jobservice!=null && i<m_jobservice.length; i++) {
            Jobservice job = m_jobservice[i];

            // get attributes
            ConfAttributesMap attrs = new ConfAttributesMap(job.getAttribute());

            // update configured attributes with usages
            Usage usage = desc.getUsage(job.getType());
            if (usage != null) {
                usage.updateAttributes(attrs.getMap());
            }

            // set new attributes
            job.setAttribute(attrs.toArray());

            // update configured attributes with user attributes
            this.updateAttributes(userAttributes, job, job.getPath());
            for (int a=0; a<job.getPathAliasCount(); a++) {
                this.updateAttributes(userAttributes, job, job.getPathAlias(a));
            }
        }
    }
    private void updateAttributes(UserAttributesMap userAttributes, Jobservice job, String id) {
        Attribute[] attributes = userAttributes.update(job.getAttribute(), id);
        if (attributes != null) {
            job.setAttribute(attributes);
        }
    }

    public Jobservice findJobservice(String path) throws NoSuccess {
        for (int j=0; j<m_jobservice.length; j++) {
            Jobservice service = m_jobservice[j];
            if (service.getPath().equals(path)) {
                return service;
            }
        }
        throw new NoSuccess("No job-service matches path: "+path);
    }

    public Jobservice[] toXMLArray() {
        return m_jobservice;
    }
}
