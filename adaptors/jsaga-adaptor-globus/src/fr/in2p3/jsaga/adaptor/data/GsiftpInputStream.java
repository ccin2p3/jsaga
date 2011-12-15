package fr.in2p3.jsaga.adaptor.data;

import org.globus.common.ChainedIOException;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.FTPInputStream;

import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpInputStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class GsiftpInputStream extends FTPInputStream {
    public GsiftpInputStream(GsiftpClient client, String file) throws IOException, FTPException {
        super.ftp = client;
        boolean passive = true;
        super.get(passive, Session.TYPE_IMAGE, file);
    }

}
