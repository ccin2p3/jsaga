package fr.in2p3.jsaga.engine.descriptors;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.engine.schema.config.Context;
import org.ogf.saga.error.NoSuccessException;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptorDescriptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SecurityAdaptorDescriptor {
    private Map m_adaptorClasses;
    private Map m_credentialClasses;
    private Map m_usages;
    protected Context[] m_xml;

    public SecurityAdaptorDescriptor(Class[] adaptorClasses) throws IllegalAccessException, InstantiationException {
        m_adaptorClasses = new HashMap();
        m_credentialClasses = new HashMap();
        m_usages = new HashMap();
        m_xml = new Context[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            SecurityAdaptor adaptor = (SecurityAdaptor) adaptorClasses[i].newInstance();

            // type
            m_adaptorClasses.put(adaptor.getType(), adaptorClasses[i]);
            m_credentialClasses.put(adaptor.getType(), adaptor.getSecurityCredentialClass());
            Usage usage = adaptor.getUsage();
            if (usage != null) {
                m_usages.put(adaptor.getType(), usage);
            }
            m_xml[i] = toXML(adaptor);
        }
    }

    public Class getAdaptorClass(String type) throws NoSuccessException {
        Class clazz = (Class) m_adaptorClasses.get(type);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccessException("Found no security adaptor supporting type: "+type);
        }
    }

    public Usage getUsage(String type) {
        return (Usage) m_usages.get(type);
    }

    public String[] getSupportedContextTypes(Class[] supportedSecurityAdaptorClasses) {
        Set contextTypeSet = new HashSet();
        for (Iterator it= m_credentialClasses.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String contextType = (String) entry.getKey();
            Class securityAdaptorClazz = (Class) entry.getValue();
            if (isSupported(securityAdaptorClazz, supportedSecurityAdaptorClasses)) {
                contextTypeSet.add(contextType);
            }
        }
        if (isSupportedNoContext(supportedSecurityAdaptorClasses)) {
            contextTypeSet.add("None");
        }
        return (String[]) contextTypeSet.toArray(new String[contextTypeSet.size()]);
    }

    public static boolean isSupportedNoContext(Class[] supportedClazzArray) {
        if (supportedClazzArray!=null && supportedClazzArray.length>0) {
            for (int i=0; supportedClazzArray!=null && i<supportedClazzArray.length; i++) {
                if (supportedClazzArray[i] == null) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean isSupported(Class securityAdaptorClazz, Class[] supportedClazzArray) {
        for (int i=0; supportedClazzArray!=null && i<supportedClazzArray.length; i++) {
            if (superClassesContain(securityAdaptorClazz, supportedClazzArray[i])) {
                return true;
            }
        }
        return false;
    }
    private static boolean superClassesContain(Class securityAdaptorClazz, Class supportedClazz) {
        if (securityAdaptorClazz.equals(supportedClazz)) {
            // stop condition
            return true;
        } else {
            // recurse
            Class superClazz = securityAdaptorClazz.getSuperclass();
            if (superClazz != null) {
                return superClassesContain(superClazz, supportedClazz);
            } else {
                return false;
            }
        }
    }

    private static Context toXML(SecurityAdaptor adaptor) {
        Context ctx = new Context();
        ctx.setName(adaptor.getType()); // default identifier
        ctx.setType(adaptor.getType());
        ctx.setImpl(adaptor.getClass().getName());
        if (adaptor.getUsage() != null) {
            ctx.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(ctx, adaptor);
        return ctx;
    }
}
