package fr.in2p3.jsaga.engine.adaptor;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.schema.config.Language;

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
 *
 */
public class LanguageAdaptorFactory {
    public static LanguageAdaptor getLanguageAdaptor(String name) throws Exception {
        Language config = new Language();
        config.setName(name);
        config.setParse("fr.in2p3.jsaga.adaptor.language.JSDLLanguageAdaptor");
        
        // Get config parameters
        Class clazz = Class.forName(config.getParse());

        // Create instance
        LanguageAdaptor parser = (LanguageAdaptor) clazz.newInstance();
        parser.loadLanguageDefinitionResources();
        return parser;
    }
}
