package fr.in2p3.jsaga.adaptor.data;

import java.io.IOException;
import java.io.OutputStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverOutputStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverOutputStream extends OutputStream {
    public void write(int i) throws IOException {
        WaitForEverDataAdaptorAbstract.hang();
    }
}
