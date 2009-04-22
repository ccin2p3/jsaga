package fr.in2p3.jsaga.adaptor.data;

import org.ogf.saga.error.NoSuccessException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMResponse
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   22 avr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SRMResponse {
    private String m_token;
    private java.net.URI m_transferUrl;

    public SRMResponse(String token, org.apache.axis.types.URI transferUrl) throws NoSuccessException {
        // set token
        m_token = token;

        //todo: remove this workaround when the bug will be fixed in DPM
        try {
            if (transferUrl.getPath()!=null && transferUrl.getPath().indexOf(':')>-1) {
                transferUrl.setPath(transferUrl.getPath().substring(transferUrl.getPath().indexOf(':')+1));
            }
            if (transferUrl.getPort() == -1) {
                transferUrl.setPort(2811);
            }
        } catch (org.apache.axis.types.URI.MalformedURIException e) {
            throw new NoSuccessException("INTERNAL ERROR: failed to correct transfer URI: "+transferUrl);
        }

        // set transfer URL
        try {
            m_transferUrl = new java.net.URI(transferUrl.toString());
        } catch (java.net.URISyntaxException e) {
            throw new NoSuccessException("INTERNAL ERROR: failed to convert transfer URI: "+transferUrl);
        }
    }

    public String getToken() {
        return m_token;
    }

    public java.net.URI getTransferUrl() {
        return m_transferUrl;
    }
}
