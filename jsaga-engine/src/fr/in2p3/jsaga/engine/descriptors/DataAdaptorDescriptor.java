package fr.in2p3.jsaga.engine.descriptors;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import org.ogf.saga.error.IncorrectURLException;
import org.ogf.saga.error.NoSuccessException;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataAdaptorDescriptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataAdaptorDescriptor {
    private Map m_classes;
    private Map m_usages;
    private Map<String,Map> m_defaults;
    protected Protocol[] m_xml;

    public DataAdaptorDescriptor(Class[] adaptorClasses, SecurityAdaptorDescriptor securityDesc) throws IllegalAccessException, InstantiationException, IncorrectURLException {
        m_classes = new HashMap();
        m_usages = new HashMap();
        m_defaults = new HashMap<String,Map>();
        m_xml = new Protocol[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            DataAdaptor adaptor = (DataAdaptor) adaptorClasses[i].newInstance();
            if (adaptor.getType() == null) {
                throw new InstantiationException("Bad adaptor: no type defined");
            }

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

    public Class getClass(String scheme) throws NoSuccessException {
        Class clazz = (Class) m_classes.get(scheme);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccessException("Found no data adaptor supporting scheme: "+ scheme);
        }
    }

    public Usage getUsage(String scheme) {
        return (Usage) m_usages.get(scheme);
    }

    public Map getDefaultsMap(String scheme) {
        return m_defaults.get(scheme);
    }

    public Protocol[] getXML() {
        return m_xml;
    }

    private static Protocol toXML(DataAdaptor adaptor, SecurityAdaptorDescriptor securityDesc) throws IncorrectURLException {
        Protocol protocol = new Protocol();
        protocol.setRead(adaptor instanceof DataReaderAdaptor);
        protocol.setWrite(adaptor instanceof DataWriterAdaptor);
        protocol.setThirdparty(adaptor instanceof DataCopy || adaptor instanceof DataCopyDelegated);
        protocol.setLogical(adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter);

        // add default data service
        protocol.setType(adaptor.getType());
        protocol.setImpl(adaptor.getClass().getName());
        if (adaptor.getSupportedSecurityCredentialClasses() != null) {
            String[] supportedContextTypes = securityDesc.getSupportedContextTypes(adaptor.getSupportedSecurityCredentialClasses());
            protocol.setSupportedContextType(supportedContextTypes);
        }
        if (adaptor.getUsage() != null) {
            protocol.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(protocol, adaptor);
        return protocol;
    }
}
