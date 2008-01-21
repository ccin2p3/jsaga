package fr.in2p3.jsaga.engine.config.adaptor;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.schema.config.Language;
import org.ogf.saga.error.NoSuccess;

import java.util.HashMap;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LanguageAdaptorDescriptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LanguageAdaptorDescriptor {
    private Map m_classes;
    protected Language[] m_xml;

    public LanguageAdaptorDescriptor(Class[] adaptorClasses) throws IllegalAccessException, InstantiationException {
        m_classes = new HashMap();
        m_xml = new Language[adaptorClasses.length];
        for (int i=0; i<adaptorClasses.length; i++) {
            LanguageAdaptor adaptor = (LanguageAdaptor) adaptorClasses[i].newInstance();
            m_classes.put(adaptor.getName(), adaptorClasses[i]);
            m_xml[i] = toXML(adaptor);
        }
    }

    public Class getClass(String language) throws NoSuccess {
        Class clazz = (Class) m_classes.get(language);
        if (clazz != null) {
            return clazz;
        } else {
            throw new NoSuccess("Found no language adaptor supporting language: "+ language);
        }
    }

    private static Language toXML(LanguageAdaptor adaptor) {
        Language language = new Language();
        language.setName(adaptor.getName());
        language.setImpl(adaptor.getClass().getName());
        return language;
    }
}
