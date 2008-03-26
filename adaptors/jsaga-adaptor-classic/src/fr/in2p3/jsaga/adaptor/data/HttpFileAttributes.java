package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpFileAttributes
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpFileAttributes extends FileAttributes {
    public HttpFileAttributes(Element img) {
        boolean isDir = img.getAttribute("alt").equals("[DIR]");

        Node n = img;
        while((n=n.getNextSibling()).getNodeType() != Node.ELEMENT_NODE);
        Element a = (Element) n;
        String href = a.getAttribute("href");
        m_name = isDir
                ? href.substring(0, href.length()-1)
                : href;

        m_type = isDir
                ? FileAttributes.DIRECTORY_TYPE
                : FileAttributes.FILE_TYPE;
    }
}
