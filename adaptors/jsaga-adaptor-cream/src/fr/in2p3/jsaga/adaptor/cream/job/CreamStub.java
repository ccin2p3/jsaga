package fr.in2p3.jsaga.adaptor.cream.job;

import org.apache.axis.types.URI;
import org.glite.ce.creamapi.ws.cream2.CREAMLocator;
import org.glite.ce.creamapi.ws.cream2.CREAMPort;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamStub
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 d�c. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamStub {
    private URL m_url;
    private CREAMPort m_stub;

    public CreamStub(String host, int port) throws BadParameterException, NoSuccessException {
        try {
            m_url = new URL("https", host, port, "/ce-cream/services/CREAM2");
        } catch (MalformedURLException e) {
            throw new BadParameterException(e.getMessage(), e);
        }

        // create stub
        try {
            if (m_url.getProtocol().startsWith("https")) {
                File proxyFile = new File(System.getProperty("java.io.tmpdir"), "dlgor_"+System.getProperty("user.name"));
                System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
                System.setProperty("gridProxyFile", proxyFile.getAbsolutePath());
            }
            CREAMLocator creamLocator = new CREAMLocator();
            m_stub = creamLocator.getCREAM2(m_url);
        } catch (ServiceException e) {
            throw new NoSuccessException(e);
        }
    }

    public CREAMPort getStub() {
        return m_stub;
    }

    public URI getURI() throws NoSuccessException {
        try {
            return new URI(m_url.getProtocol(), m_url.getUserInfo(), m_url.getHost(), m_url.getPort(), m_url.getPath(), m_url.getQuery(), m_url.getRef());
        } catch (URI.MalformedURIException e) {
            throw new NoSuccessException(e);
        }
    }
}
