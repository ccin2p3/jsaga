package fr.in2p3.jsaga.adaptor.job;

import org.globus.io.gass.server.JobOutputListener;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GatekeeperJobOutputListener
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 mai 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GatekeeperJobOutputListener implements JobOutputListener {
    private OutputStream m_outputStream;

    public GatekeeperJobOutputListener(OutputStream stdout) {
        m_outputStream = stdout;
    }

    public void outputChanged(String s) {
        if (m_outputStream != null) {
            try {
                m_outputStream.write(s.getBytes());
            } catch (IOException e) {
            }
        } else {
            //todo: save to buffer
        }
    }

    public void outputClosed() {
        if (m_outputStream != null) {
            try {
                m_outputStream.close();
            } catch (IOException e) {
            }
        } else {
            //todo
        }
    }
}
