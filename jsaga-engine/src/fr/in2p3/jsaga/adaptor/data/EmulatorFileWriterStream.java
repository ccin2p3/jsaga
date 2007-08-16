package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionAbstract;
import fr.in2p3.jsaga.adaptor.data.write.FileWriterStream;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.File;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorFileWriterStream
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   5 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorFileWriterStream implements FileWriterStream {
    private DataEmulatorConnectionAbstract m_server;
    private File m_file;

    public EmulatorFileWriterStream(DataEmulatorConnectionAbstract server, File file) {
        m_server = server;
        m_file = file;
    }

    public int write(byte[] buffer, int offset, int len) throws Timeout {
        StringBuffer b = new StringBuffer();
        if (m_file.getContent() != null) {
            b.append(m_file.getContent());
        }
        if (offset==0 && len==buffer.length) {
            b.append(new String(buffer));
        } else {
            byte[] array = new byte[len-offset];
            System.arraycopy(buffer, offset, array, 0, len);
            b.append(new String(array));
        }
        m_file.setContent(b.toString());
        try {
            m_server.commit();
        } catch (NoSuccess e) {
            throw new Timeout(e);
        }
        return len;
    }

    public void close() throws IncorrectState {
    }
}
