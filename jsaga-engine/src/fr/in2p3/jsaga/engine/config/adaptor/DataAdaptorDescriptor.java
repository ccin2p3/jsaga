package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopy;
import fr.in2p3.jsaga.adaptor.data.optimise.DataCopyDelegated;
import fr.in2p3.jsaga.adaptor.data.read.DataReaderAdaptor;
import fr.in2p3.jsaga.adaptor.data.read.LogicalReader;
import fr.in2p3.jsaga.adaptor.data.write.DataWriterAdaptor;
import fr.in2p3.jsaga.adaptor.data.write.LogicalWriter;
import fr.in2p3.jsaga.engine.schema.config.Protocol;
import org.ogf.saga.error.NoSuccess;

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
    protected Protocol[] m_xml;

    public DataAdaptorDescriptor(Class[] adaptorClasses, SecurityAdaptorDescriptor securityDesc) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_usages = new HashMap();
        m_xml = new Protocol[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            DataAdaptor adaptor = (DataAdaptor) adaptorClasses[i].newInstance();
            if (adaptor.getSchemeAliases()==null || adaptor.getSchemeAliases().length==0) {
                throw new InstantiationException("Bad adaptor: no scheme defined");
            }

            // map main scheme to adaptor usage (if it exist)
            Usage usage = adaptor.getUsage();
            if (usage != null) {
                m_usages.put(adaptor.getSchemeAliases()[0], usage);
            }

            // convert adaptor to XML
            m_xml[i] = toXML(adaptor, securityDesc);

            // map all scheme aliases to adaptor class
            String[] alias = adaptor.getSchemeAliases();
            for (int a=0; alias!=null && a<alias.length; a++) {
                m_classes.put(alias[a], adaptorClasses[i]);
            }
        }
    }

    public Class getClass(String scheme) throws NoSuccess {
        Class clazz = (Class) m_classes.get(scheme);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccess("Found no data adaptor supporting scheme: "+ scheme);
        }
    }

    public Usage getUsage(String scheme) {
        return (Usage) m_usages.get(scheme);
    }

    private static Protocol toXML(DataAdaptor adaptor, SecurityAdaptorDescriptor securityDesc) {
        Protocol protocol = new Protocol();
        protocol.setScheme(adaptor.getSchemeAliases()[0]);
        protocol.setImpl(adaptor.getClass().getName());
        protocol.setRead(adaptor instanceof DataReaderAdaptor);
        protocol.setWrite(adaptor instanceof DataWriterAdaptor);
        protocol.setThirdparty(adaptor instanceof DataCopy || adaptor instanceof DataCopyDelegated);
        protocol.setLogical(adaptor instanceof LogicalReader || adaptor instanceof LogicalWriter);
        if (adaptor.getSchemeAliases() != null) {
            String[] schemeAndSchemeAliases = adaptor.getSchemeAliases();
            String[] schemeAliases = new String[schemeAndSchemeAliases.length-1];
            System.arraycopy(schemeAndSchemeAliases, 1, schemeAliases, 0, schemeAliases.length);
            protocol.setSchemeAlias(schemeAliases);
        }
        if (adaptor.getSupportedSecurityAdaptorClasses() != null) {
            String[] supportedContextTypes = securityDesc.getSupportedContextTypes(adaptor.getSupportedSecurityAdaptorClasses());
            protocol.setSupportedContextType(supportedContextTypes);
        }
        if (adaptor.getUsage() != null) {
            protocol.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(protocol, adaptor);
        return protocol;
    }
}
