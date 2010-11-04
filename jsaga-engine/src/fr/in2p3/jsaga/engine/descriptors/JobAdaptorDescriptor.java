package fr.in2p3.jsaga.engine.descriptors;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.JobControlAdaptor;
import fr.in2p3.jsaga.adaptor.job.monitor.JobMonitorAdaptor;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.engine.schema.config.types.AttributeSourceType;
import org.ogf.saga.error.NoSuccessException;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobAdaptorDescriptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobAdaptorDescriptor {
    private Map m_classes;
    private Map m_usages;
    private Map<String,Map> m_defaults;
    protected Execution[] m_xml;

    public JobAdaptorDescriptor(Class[] adaptorClasses, SecurityAdaptorDescriptor securityDesc) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_usages = new HashMap();
        m_defaults = new HashMap<String,Map>();
        m_xml = new Execution[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            JobControlAdaptor adaptor = (JobControlAdaptor) adaptorClasses[i].newInstance();

            // set class
            m_classes.put(adaptor.getType(), adaptorClasses[i]);

            // set usage
            Usage usage = adaptor.getUsage();
            if (usage != null) {
                m_usages.put(adaptor.getType(), usage);
            }

            // set defaults
            m_defaults.put(adaptor.getType(), AdaptorDescriptors.getDefaultsMap(adaptor));

            // set XML
            m_xml[i] = toXML(adaptor, securityDesc);
        }
    }

    public Class getClass(String type) throws NoSuccessException {
        Class clazz = (Class) m_classes.get(type);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccessException("Found no job adaptor supporting type: "+type);
        }
    }

    public Usage getUsage(String type) {
        return (Usage) m_usages.get(type);
    }

    public Map getDefaultsMap(String scheme) {
        return m_defaults.get(scheme);
    }

    public Execution[] getXML() {
        return m_xml;
    }

    private static Execution toXML(JobControlAdaptor adaptor, SecurityAdaptorDescriptor securityDesc) {
        Execution execution = new Execution();

        // add default job service
        execution.setType(adaptor.getType());
        execution.setImpl(adaptor.getClass().getName());
        if (adaptor.getSupportedSecurityCredentialClasses() != null) {
            String[] supportedContextTypes = securityDesc.getSupportedContextTypes(adaptor.getSupportedSecurityCredentialClasses());
            execution.setSupportedContextType(supportedContextTypes);
        }
        JobMonitorAdaptor monitorAdaptor = adaptor.getDefaultJobMonitor();
        if (monitorAdaptor != null) {
            MonitorService monitor = new MonitorService();
            monitor.setImpl(monitorAdaptor.getClass().getName());
            AdaptorDescriptors.setDefaults(monitor, monitorAdaptor);
            execution.setMonitorService(monitor);
        }
        if (adaptor.getUsage() != null) {
            execution.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(execution, adaptor);
        String checkAvailability  = EngineProperties.getProperty(EngineProperties.JOB_CONTROL_CHECK_AVAILABILITY);
        if (checkAvailability != null) {
            Attribute attr = new Attribute();
            attr.setName(JobAdaptor.CHECK_AVAILABILITY);
            attr.setValue(checkAvailability);
            attr.setSource(AttributeSourceType.ENGINECONFIGURATION);
            execution.addAttribute(attr);
        }
        return execution;
    }
}
