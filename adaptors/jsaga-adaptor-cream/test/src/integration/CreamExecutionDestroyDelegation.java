package integration;

import fr.in2p3.jsaga.adaptor.cream.job.DelegationStub;
import org.ogf.saga.AbstractTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

import java.io.File;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionDestroyDelegation
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 fevr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionDestroyDelegation extends AbstractTest {
    private URL m_url;
    private String m_delegationId;

    public CreamExecutionDestroyDelegation() throws Exception {
        this("cream");
    }
    protected CreamExecutionDestroyDelegation(String jobprotocol) throws Exception {
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

    // TODO
//    public void test_destroy() throws Exception {
//        System.setProperty("sslCAFiles", new File(new File(new File(System.getProperty("user.home"),".globus"),"certificates"),"*.0").getAbsolutePath());
//
//        DelegationStub stub = new DelegationStub(m_url.getHost(), m_url.getPort(), DelegationStub.ANY_VO);
//        stub.getStub().destroy(m_delegationId);
//    }
}
