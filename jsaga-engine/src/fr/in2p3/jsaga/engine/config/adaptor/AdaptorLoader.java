package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.engine.config.ConfigurationException;

import java.net.URL;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AdaptorLoader
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class AdaptorLoader {
    private static final String ADAPTOR_PROPERTIES = "META-INF/adaptor.properties";
    private Class[] m_adaptorClasses;

    public AdaptorLoader() throws ConfigurationException {
        try {
            List list = new ArrayList();
            Enumeration e = AdaptorLoader.class.getClassLoader().getResources(ADAPTOR_PROPERTIES);
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                Properties prop = new Properties();
                prop.load(url.openStream());
                for (Enumeration e2=prop.propertyNames(); e2.hasMoreElements(); ) {
                    String clazzName = (String) e2.nextElement();
                    Class clazz = Class.forName(clazzName);
                    list.add(clazz);
                }
            }
            m_adaptorClasses = (Class[]) list.toArray(new Class[list.size()]);
        } catch(Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public Class[] getClasses(Class itf) {
        List list = new ArrayList();
        for (int i=0; i<m_adaptorClasses.length; i++) {
            Class current = m_adaptorClasses[i];
            if (hasInterface(current, itf)) {
                list.add(current);
            }
        }
        return (Class[]) list.toArray(new Class[list.size()]);
    }

    private static boolean hasInterface(Class clazz, Class itf) {
        Set itfSet = new HashSet();
        listInterfaces(clazz, itfSet);
        return itfSet.contains(itf);
    }
    
    private static void listInterfaces(Class clazz, Set set) {
        // list interfaces of implemented interfaces
        Class[] interfaces = clazz.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            set.add(interfaces[i]);
            listInterfaces(interfaces[i], set);
        }
        // list interfaces of extended class
        Class extended = clazz.getSuperclass();
        if (extended != null) {
            listInterfaces(extended, set);
        }
    }
}
