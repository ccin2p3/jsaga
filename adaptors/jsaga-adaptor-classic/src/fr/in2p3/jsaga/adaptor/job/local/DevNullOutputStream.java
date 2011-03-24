package fr.in2p3.jsaga.adaptor.job.local;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DevNullOutputStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DevNullOutputStream extends OutputStream {
    @Override
    public void write(int i) throws IOException {
        // do nothing
    }
}
