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
