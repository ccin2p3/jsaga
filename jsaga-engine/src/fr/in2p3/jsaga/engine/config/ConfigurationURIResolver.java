package fr.in2p3.jsaga.engine.config;

import javax.xml.transform.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ConfigurationURIResolver
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ConfigurationURIResolver implements URIResolver {
    private Source m_source;

    public ConfigurationURIResolver(Source source) {
        m_source = source;
    }

    public Source resolve(String href, String base) throws TransformerException {
        return m_source;
    }
}
