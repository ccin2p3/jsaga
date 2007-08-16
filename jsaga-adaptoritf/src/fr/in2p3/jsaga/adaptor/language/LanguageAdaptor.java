package fr.in2p3.jsaga.adaptor.language;

import org.ogf.saga.error.BadParameter;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LanguageAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LanguageAdaptor {
    public void loadLanguageDefinitionResources() throws IOException;
    public Document jobDescriptionToDOM(InputStream xmlStream) throws BadParameter;
}
