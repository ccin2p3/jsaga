package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HtmlDataAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class HtmlDataAdaptorAbstract implements FileReaderStreamFactory {
    protected URL m_baseUrl;

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set base URL
        try {
            m_baseUrl = new URL(this.getNativeScheme(), host, port, "/");
        } catch (MalformedURLException e) {
            throw new BadParameterException(e);
        }
    }

    public void disconnect() throws NoSuccessException {
        // unset base URL
        m_baseUrl = null;
    }

    public abstract String getNativeScheme();
}
