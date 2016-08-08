package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionAbstract;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.File;
import org.ogf.saga.error.NoSuccessException;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorOutputStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorOutputStream extends OutputStream {
    private DataEmulatorConnectionAbstract m_server;
    private File m_file;

    public EmulatorOutputStream(DataEmulatorConnectionAbstract server, File file) {
        m_server = server;
        m_file = file;
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        StringBuffer b = new StringBuffer();
        if (m_file.getContent() != null) {
            b.append(m_file.getContent());
        }
        if (off==0 && len==bytes.length) {
            b.append(new String(bytes));
        } else {
            byte[] array = new byte[len-off];
            System.arraycopy(bytes, off, array, 0, len);
            b.append(new String(array));
        }
        m_file.setContent(b.toString());
        try {
            if(Base.DEBUG) m_server.commit();
        } catch (NoSuccessException e) {
            throw new IOException("Failed to commit modification");
        }
    }

    public void write(int i) throws IOException {
        StringBuffer b = new StringBuffer();
        if (m_file.getContent() != null) {
            b.append(m_file.getContent());
        }
        b.append((char) i);
        m_file.setContent(b.toString());
/*
        try {
            if(Base.DEBUG) m_server.commit();
        } catch (NoSuccessException e) {
            throw new IOException("Failed to commit modification");
        }
*/
    }

    public void close() throws IOException {
        try {
            m_server.commit();
        } catch (NoSuccessException e) {
            throw new IOException("Failed to commit modification");
        }
    }
}
