package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.adaptor.LanguageAdaptorDescriptor;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LanguageAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 mai 2007
* ***************************************************
* Description:                                      */
/**
 * Create and manage language adaptors
 */
public class LanguageAdaptorFactory {
    private LanguageAdaptorDescriptor m_descriptor;

    public LanguageAdaptorFactory(Configuration configuration) {
        m_descriptor = configuration.getDescriptors().getLanguageDesc();
    }

    /**
     * Create a new instance of language adaptor for <code>language</code>.
     * @param language the name of the language
     * @return the language adaptor instance
     */
    public LanguageAdaptor getLanguageAdaptor(String language) throws NoSuccess {
        // create instance
        Class clazz = m_descriptor.getClass(language);
        LanguageAdaptor adaptor;
        try {
            adaptor = (LanguageAdaptor) clazz.newInstance();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // initialize adaptor
        try {
            adaptor.initParser();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return adaptor;
    }
}
