package integration;

import fr.in2p3.jsaga.adaptor.cream.CreamSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamAbstractTest
* Author: lionel.schwarz@in2p3.fr
* Date:   10 déc 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class CreamAbstractTest extends AbstractTest {
    protected URL m_url;
    protected String m_delegationId;

    public CreamAbstractTest() throws Exception {
        this("cream");
    }
    protected CreamAbstractTest(String jobprotocol) throws Exception {
        super();
        m_url = URLFactory.createURL(getRequiredProperty(jobprotocol, CONFIG_JOBSERVICE_URL));
        String query = m_url.getQuery();
        if (query!=null && query.startsWith("delegationId=")) {
            m_delegationId = query.substring(query.indexOf('=')+1);
        } else {
            String dn = "/O=GRID-FR/C=FR/O=CNRS/OU=CC-LYON/CN=Sylvain Reynaud";
            m_delegationId = "delegation-"+Math.abs(dn.hashCode());
        }
        // Set the SSL context
        Protocol.registerProtocol("https", 
            new Protocol("https", (ProtocolSocketFactory)new CreamSocketFactory(
                new File(
                    new File(
                        new File(System.getProperty("user.home"), ".jsaga"),
                    "tmp"),
                "voms_cred.txt")
                .getAbsolutePath(), 
                new File(
                    new File(
                        new File(
                            new File(System.getProperty("user.home"), ".jsaga"),
                        "contexts"),
                    "voms"),
                "certificates")), 
                m_url.getPort()
            )
        );
    }

}
