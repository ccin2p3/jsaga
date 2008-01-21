package fr.in2p3.jsaga.engine.jobcollection.preprocess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XMLDocument
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 mai 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XMLDocument {
    private byte[] m_content;
    private File m_file;

    public XMLDocument(File file) {
        m_content = null;
        m_file = file;
    }

    public void set(byte[] content) {
        m_content = content;
    }
    public byte[] get() {
        return m_content;
    }

    public void save() throws IOException {
        if (m_content != null) {
            OutputStream out = new FileOutputStream(m_file);
            out.write(m_content);
            out.close();
        }
    }
}
