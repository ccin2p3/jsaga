package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.BulkJobSubmit;
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
    protected Execution[] m_xml;

    public JobAdaptorDescriptor(Class[] adaptorClasses, SecurityAdaptorDescriptor securityDesc) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_usages = new HashMap();
        m_xml = new Execution[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            JobAdaptor adaptor = (JobAdaptor) adaptorClasses[i].newInstance();

            // type
            m_classes.put(adaptor.getType(), adaptorClasses[i]);
            Usage usage = adaptor.getUsage();
            if (usage != null) {
                m_usages.put(adaptor.getType(), usage);
            }
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

    private static Execution toXML(JobAdaptor adaptor, SecurityAdaptorDescriptor securityDesc) {
        Execution execution = new Execution();
        execution.setScheme(adaptor.getType()); // default identifier
        execution.setBulk(adaptor instanceof BulkJobSubmit);

        // add default job service
        JobService service = new JobService();
        service.setName("default");
        service.setType(adaptor.getType());
        service.setImpl(adaptor.getClass().getName());
        if (adaptor.getSupportedSecurityAdaptorClasses() != null) {
            String[] supportedContextTypes = securityDesc.getSupportedContextTypes(adaptor.getSupportedSecurityAdaptorClasses());
            service.setSupportedContextType(supportedContextTypes);
        }
        JobMonitorAdaptor monitorAdaptor = adaptor.getDefaultJobMonitor();
        if (monitorAdaptor != null) {
            MonitorService monitor = new MonitorService();
            monitor.setImpl(monitorAdaptor.getClass().getName());
            AdaptorDescriptors.setDefaults(monitor, monitorAdaptor);
            service.setMonitorService(monitor);
        }
        if (adaptor.getUsage() != null) {
            service.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(service, adaptor);
        String checkAvailability  = EngineProperties.getProperty(EngineProperties.JOB_CONTROL_CHECK_AVAILABILITY);
        if (checkAvailability != null) {
            Attribute attr = new Attribute();
            attr.setName(JobAdaptor.CHECK_AVAILABILITY);
            attr.setValue(checkAvailability);
            attr.setSource(AttributeSourceType.ENGINECONFIGURATION);
            service.addAttribute(attr);
        }
        execution.addJobService(service);
        return execution;
    }
}
