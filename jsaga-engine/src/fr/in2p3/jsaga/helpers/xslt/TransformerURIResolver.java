package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.config.Configuration;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TransformerURIResolver
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TransformerURIResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
        if ("var/jsaga-merged-config.xml".equals(href)) {
            try {
                return new DOMSource(Configuration.getInstance().getConfigurations().getAsDocument());
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        } else {
            return new StreamSource(new File(Base.JSAGA_HOME, href));
        }
    }
}
