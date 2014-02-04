package integration;

import fr.in2p3.jsaga.adaptor.cream.CreamSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.AbstractTest_JUNIT4;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;
import java.util.Date;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamAbstractTest
* Author: lionel.schwarz@in2p3.fr
* Date:   10 d�c 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class CreamAbstractTest extends AbstractTest_JUNIT4 {
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
    }

    /** Implicitly invoked before executing each test method */
    @BeforeClass
    public static void registerProtocol() throws Exception {
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
                8443
            )
        );
    }
        
    /** Implicitly invoked after executing each test method */
    @AfterClass
    public static void unregisterProtocol() throws Exception {
        Protocol.unregisterProtocol("https");
    }

}
