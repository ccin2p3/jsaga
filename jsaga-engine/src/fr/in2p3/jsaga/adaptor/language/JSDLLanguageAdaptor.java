package fr.in2p3.jsaga.adaptor.language;

import fr.in2p3.jsaga.adaptor.language.abstracts.AbstractLanguageAdaptorXML;

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
public class JSDLLanguageAdaptor extends AbstractLanguageAdaptorXML implements LanguageAdaptor {
    public String getName() {
        return "JSDL";
    }

    public void initParser() throws Exception {
        super._initParser(new String[]{
                "schema/jsdl-extended.xsd.xml",
                "schema/jsdl-extension.xsd",
                "schema/jsdl-posix.xsd",
                "schema/jsdl-spmd.xsd",
                "schema/jsdl.xsd"});
    }

    public String getTranslator() {
        return null;
    }
}
