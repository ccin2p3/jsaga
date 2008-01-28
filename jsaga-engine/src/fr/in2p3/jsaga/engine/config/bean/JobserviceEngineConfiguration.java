package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.UserAttributesMap;
import fr.in2p3.jsaga.engine.config.adaptor.JobAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.*;

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

            // correct configured attributes according to usage
            Map<String,Integer> weights = new HashMap<String,Integer>();
            for (int a=0; a<job.getAttributeCount(); a++) {
                Attribute attr = job.getAttribute(a);
                weights.put(attr.getName(), attr.getSource().getType());
            }
            Usage usage = desc.getUsage(job.getType());
            if (usage != null) {
                usage.setWeight(weights);
                for (int a=0; a<job.getAttributeCount(); a++) {
                    Attribute attr = job.getAttribute(a);
                    try {
                        String correctedValue = usage.correctValue(attr.getName(), attr.getValue());
                        attr.setValue(correctedValue);
                    } catch(DoesNotExist e) {
                        // do nothing
                    }
                }
            }

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

    public Jobservice[] toXMLArray() {
        return m_jobservice;
    }

    public Jobservice findJobservice(String name) throws NoSuccess {
        for (int j=0; j<m_jobservice.length; j++) {
            Jobservice service = m_jobservice[j];
            if (service.getName().equals(name)) {
                return service;
            }
        }
        throw new NoSuccess("No job-service matches name: "+name);
    }

    /**
     * Find the context to be used with <code>url</code>
     */
    public ContextInstanceRef[] listContextInstanceCandidates(URL url) throws NotImplemented, BadParameter, NoSuccess {
        if (url != null) {
            return this.listContextInstanceCandidates(
                    findJobservice(url.getScheme()),
                    url.getHost(),
                    url.getFragment());
        } else {
            throw new BadParameter("URL is null");
        }
    }

    public ContextInstanceRef[] listContextInstanceCandidates(Jobservice service, String hostname, String fragment) throws NoSuccess {
        ContextEngineConfiguration config = Configuration.getInstance().getConfigurations().getContextCfg();
        if (fragment != null) {
            ContextInstance[] ctxArray = config.listContextInstanceArrayById(fragment);
            switch(ctxArray.length) {
                case 0:
                    throw new NoSuccess("No context instance matches: "+fragment);
                case 1:
                    return new ContextInstanceRef[]{toContextInstanceRef(ctxArray[0])};
                default:
                    return toContextInstanceRefArray(ctxArray);
            }
        } else if (service.getContextInstanceRefCount() > 0) {
            // if no context instance is specified, then all configured context instances are eligible
            return service.getContextInstanceRef();
        } else {
            // if no context instance is configured, then all supported context instances are eligible
            List list = new ArrayList();
            for (int c=0; c<service.getSupportedContextTypeCount(); c++) {
                String type = service.getSupportedContextType(c);
                ContextInstance[] ctxArray = config.listContextInstanceArray(type);
                ContextInstanceRef[] refArray = toContextInstanceRefArray(ctxArray);
                for (int i=0; i<refArray.length; i++) {
                    list.add(refArray[i]);
                }
            }
            return (ContextInstanceRef[]) list.toArray(new ContextInstanceRef[list.size()]);
        }
    }

    private static ContextInstanceRef[] toContextInstanceRefArray(ContextInstance[] ctxArray) {
        ContextInstanceRef[] refArray = new ContextInstanceRef[ctxArray.length];
        for (int i=0; i<ctxArray.length; i++) {
            refArray[i] = toContextInstanceRef(ctxArray[i]);
        }
        return refArray;
    }
    private static ContextInstanceRef toContextInstanceRef(ContextInstance ctx) {
        ContextInstanceRef ref = new ContextInstanceRef();
        ref.setType(ctx.getType());
        ref.setIndice(ctx.getIndice());
        ref.setName(ctx.getName());
        return ref;
    }
}
