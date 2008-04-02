package fr.in2p3.jsaga.adaptor.data.impl;

import java.io.IOException;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpInputStreamSocketBased
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class HttpInputStreamSocketBased extends InputStream {
    private InputStream m_response;
    private long m_nbBytesTransfered;
    private long m_nbBytesExpected;

    public HttpInputStreamSocketBased(HttpRequest request) {
        m_response = request.getInputStream();
        m_nbBytesTransfered = 0;
        m_nbBytesExpected = request.getContentLength();
    }

    public void close() throws IOException {
        m_response.close();
    }

    public int read() throws IOException {
        if (m_nbBytesExpected>=0 && m_nbBytesTransfered>=m_nbBytesExpected) {
            return -1;
        } else {
            int readlen = m_response.read();
            if (readlen > -1) {
                m_nbBytesTransfered += readlen;
            }
            return readlen;
        }
    }

    public int read(byte[] b) throws IOException {
        if (m_nbBytesExpected>=0 && m_nbBytesTransfered>=m_nbBytesExpected) {
            return -1;
        } else {
            int readlen = m_response.read(b);
            if (readlen > -1) {
                m_nbBytesTransfered += readlen;
            }
            return readlen;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (m_nbBytesExpected>=0 && m_nbBytesTransfered>=m_nbBytesExpected) {
            return -1;
        } else {
            int readlen = m_response.read(b, off, len);
            if (readlen > -1) {
                m_nbBytesTransfered += readlen;
            }
            return readlen;
        }
    }
}
