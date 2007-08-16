package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.job.JobAdaptor;
import fr.in2p3.jsaga.adaptor.job.control.ListJobControl;
import fr.in2p3.jsaga.engine.schema.config.Jobservice;
import org.ogf.saga.error.NoSuccess;

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
    protected Jobservice[] m_xml;

    public JobAdaptorDescriptor(Class[] adaptorClasses) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_xml = new Jobservice[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            JobAdaptor adaptor = (JobAdaptor) adaptorClasses[i].newInstance();
            m_classes.put(adaptor.getType(), adaptorClasses[i]);
            m_xml[i] = toXML(adaptor);
        }
    }

    public Class getClass(String type) throws NoSuccess {
        Class clazz = (Class) m_classes.get(type);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccess("Found no job adaptor supporting type: "+type);
        }
    }

    private static Jobservice toXML(JobAdaptor adaptor) {
        Jobservice jobservice = new Jobservice();
        jobservice.setType(adaptor.getType());
        jobservice.setImpl(adaptor.getClass().getName());
        jobservice.setBulk(adaptor instanceof ListJobControl);
        if (adaptor.getSupportedContextTypes() != null) {
            jobservice.setSupportedContextType(adaptor.getSupportedContextTypes());
        }
        if (adaptor.getSupportedSandboxProtocols() != null) {
            jobservice.setSupportedProtocolScheme(adaptor.getSupportedSandboxProtocols());
        }
        return jobservice;
    }
}
