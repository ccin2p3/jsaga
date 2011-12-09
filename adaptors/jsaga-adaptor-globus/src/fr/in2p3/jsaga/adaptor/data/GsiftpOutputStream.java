package fr.in2p3.jsaga.adaptor.data;

import org.globus.common.ChainedIOException;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPException;
import org.globus.io.streams.FTPOutputStream;
import org.globus.io.streams.GridFTPOutputStream;

import java.io.IOException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   GsiftpOutputStream
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   29 juin 2009
 * ***************************************************
 * Description:                                      */
/**
 * @deprecated 
 */
public class GsiftpOutputStream extends FTPOutputStream {
    //private boolean m_isClosed;

    public GsiftpOutputStream(GridFTPClient client, String file, boolean append) throws IOException, FTPException {
        //m_isClosed = false;
        this.ftp = client;
        boolean passive = true;
        put(passive, Session.TYPE_IMAGE, file, append);
    }

    public void abort() {
    	if (this.output != null) {
    	    try {
    		this.output.close();
    	    } catch(Exception e) {}
    	}
    }
    
    /** override super.close() to prevent it from closing the connection */
    //TODO: uncomment this when API will be able to reuse existing connection without hanging
    public void close() throws IOException {
    	if (this.output != null) {
    	    try {
    		this.output.close();
    	    } catch(Exception e) {}
    	}

    	try {
    	    if (this.state != null) {
    		this.state.waitForEnd();
    	    }
    	} catch (FTPException e) {
    	    throw new ChainedIOException("close failed.", e);
    	}
    }


    //todo: remove this when API will be able to reuse existing connection without hanging
    /** override super.close() to close the temporary connection once and only once */
//    public synchronized void close() throws IOException {
//        if (! m_isClosed) {
//            m_isClosed = true;
//            super.close();
//        }
//    }
}
