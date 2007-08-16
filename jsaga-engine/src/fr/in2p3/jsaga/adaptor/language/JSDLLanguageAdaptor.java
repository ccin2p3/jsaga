package fr.in2p3.jsaga.adaptor.language;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSDLLanguageAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JSDLLanguageAdaptor extends AbstractXMLLanguageAdaptor implements LanguageAdaptor {
    public void loadLanguageDefinitionResources() throws IOException {
        super.loadLanguageDefinitionResources(new String[]{
                "schema/jsdl-extended.xsd.xml",
                "schema/jsdl-extension.xsd",
                "schema/jsdl-posix.xsd-6.xsd",
                "schema/jsdl.xsd-18.xsd"});
    }
}
