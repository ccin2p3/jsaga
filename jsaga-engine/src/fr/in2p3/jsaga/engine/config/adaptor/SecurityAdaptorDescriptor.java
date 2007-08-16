package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.security.InitializableSecurityAdaptorBuilder;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import fr.in2p3.jsaga.adaptor.security.defaults.Default;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.IncorrectState;
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
    protected ContextInstance[] m_xml;

    public SecurityAdaptorDescriptor(Class[] adaptorClasses) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_xml = new ContextInstance[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            SecurityAdaptorBuilder adaptor = (SecurityAdaptorBuilder) adaptorClasses[i].newInstance();
            m_classes.put(adaptor.getType(), adaptorClasses[i]);
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

    private static ContextInstance toXML(SecurityAdaptorBuilder adaptor) {
        ContextInstance ctx = new ContextInstance();
        ctx.setType(adaptor.getType());
        ctx.setIndice(0);
        ctx.setImpl(adaptor.getClass().getName());
        ctx.setUsage(adaptor.getUsage().toString());
        if (adaptor instanceof InitializableSecurityAdaptorBuilder) {
            String usage = ((InitializableSecurityAdaptorBuilder)adaptor).getInitUsage().toString();
            Init init = new Init();
            init.setUsage(usage);
            ctx.setInit(init);
        }
        Default[] defaults;
        try {
            defaults = adaptor.getDefaults(new HashMap());
        } catch (IncorrectState e) {
            defaults = null;
        }
        if (defaults != null) {
            ctx.setAttribute(new Attribute[defaults.length]);
            for (int d=0; d<defaults.length; d++) {
                Attribute attr = new Attribute();
                attr.setName(defaults[d].getName());
                attr.setValue(defaults[d].getValue());
                ctx.setAttribute(d, attr);
            }
        }
        return ctx;
    }
}
