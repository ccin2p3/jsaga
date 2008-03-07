package fr.in2p3.jsaga;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JSagaURL
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   24 janv. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JSagaURL extends URL {
    private FileAttributes m_attributes;

    /** constructor for absolutePath */
    public JSagaURL(FileAttributes attributes, String absolutePath) throws NotImplemented, BadParameter, NoSuccess {
        super(absolutePath);
        m_attributes = attributes;
    }

    /** constructor for relativePath */
    public JSagaURL(FileAttributes attributes) throws NotImplemented, BadParameter, NoSuccess {
        super(attributes.getName().replaceAll(" ", "%20"));
        m_attributes = attributes;
    }

    public FileAttributes getAttributes() {
        return m_attributes;
    }
}
