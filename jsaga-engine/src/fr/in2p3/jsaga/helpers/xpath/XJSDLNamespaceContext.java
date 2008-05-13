package fr.in2p3.jsaga.helpers.xpath;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XJSDLNamespaceContext
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XJSDLNamespaceContext implements NamespaceContext {
    public String getNamespaceURI(String prefix){
        if("jsdl".equals(prefix)){
            return "http://schemas.ggf.org/jsdl/2005/11/jsdl";
        } else if ("ext".equals(prefix)) {
            return "http://www.in2p3.fr/jsdl-extension";
        } else {
            return null;
        }
    }

    public String getPrefix(String namespaceURI){
        if ("http://schemas.ggf.org/jsdl/2005/11/jsdl".equals(namespaceURI)) {
            return "jsdl";
        } else if ("http://www.in2p3.fr/jsdl-extension".equals(namespaceURI)) {
            return "ext";
        } else {
            return null;
        }
    }

    public Iterator getPrefixes(String namespaceURI){
        return null;
    }
}
