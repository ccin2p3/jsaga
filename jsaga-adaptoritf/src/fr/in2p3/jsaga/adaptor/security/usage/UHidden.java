package fr.in2p3.jsaga.adaptor.security.usage;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UHidden
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UHidden extends U {
    public UHidden(String name) {
        super(name);
    }

    public String toString() {
        return "*"+m_name+"*";
    }

    private volatile boolean m_stopped;
    protected String getUserInput() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        m_stopped = false;
        new Thread() {
            public void run() {
                while(!m_stopped) {
                    System.out.print("\b ");
                    try {
                        sleep(1);
                    } catch(InterruptedException e) {}
                }
            }
        }.start();
        try {
            String line = in.readLine().trim();
            if (line.length() > 0) {
                return line;
            } else {
                return null;
            }
        } catch(IOException e) {
            throw e;
        } finally {
            m_stopped = true;
        }
    }
}
