package fr.in2p3.jsaga.adaptor.data;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverInputStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class WaitForEverInputStream extends InputStream {
    public int read() throws IOException {
        WaitForEverDataAdaptorAbstract.hang();
        return 0;
    }
}
