package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.InitializableSecurityAdaptorBuilder;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.engine.schema.config.Init;
import org.ogf.saga.error.NoSuccess;

import java.util.HashMap;
import java.util.Map;

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
    private Map m_classes;
    private Map m_usages;
    private Map m_initUsages;
    protected ContextInstance[] m_xml;

    public SecurityAdaptorDescriptor(Class[] adaptorClasses) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_usages = new HashMap();
        m_initUsages = new HashMap();
        m_xml = new ContextInstance[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            SecurityAdaptorBuilder adaptor = (SecurityAdaptorBuilder) adaptorClasses[i].newInstance();

            // type
            m_classes.put(adaptor.getType(), adaptorClasses[i]);
            Usage usage = adaptor.getUsage();
            if (usage != null) {
                m_usages.put(adaptor.getType(), usage);
            }
            if (adaptor instanceof InitializableSecurityAdaptorBuilder) {
                Usage initUsage = ((InitializableSecurityAdaptorBuilder)adaptor).getInitUsage();
                if (initUsage != null) {
                    m_initUsages.put(adaptor.getType(), initUsage);
                }
            }
            m_xml[i] = toXML(adaptor);
        }
    }

    public Class getClass(String type) throws NoSuccess {
        Class clazz = (Class) m_classes.get(type);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccess("Found no security adaptor supporting type: "+type);
        }
    }

    public Usage getUsage(String type) {
        return (Usage) m_usages.get(type);
    }

    public Usage getInitUsage(String type) {
        return (Usage) m_initUsages.get(type);
    }

    private static ContextInstance toXML(SecurityAdaptorBuilder adaptor) {
        ContextInstance ctx = new ContextInstance();
        ctx.setType(adaptor.getType());
        ctx.setIndice(0);
        ctx.setImpl(adaptor.getClass().getName());
        if (adaptor instanceof InitializableSecurityAdaptorBuilder) {
            String usage = ((InitializableSecurityAdaptorBuilder)adaptor).getInitUsage().toString();
            Init init = new Init();
            init.setUsage(usage);
            ctx.setInit(init);
        }
        if (adaptor.getUsage() != null) {
            ctx.setUsage(adaptor.getUsage().toString());
        }
        AdaptorDescriptors.setDefaults(ctx, adaptor);
        return ctx;
    }
}
