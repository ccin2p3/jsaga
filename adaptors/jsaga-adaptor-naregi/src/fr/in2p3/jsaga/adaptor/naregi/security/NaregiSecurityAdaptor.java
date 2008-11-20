package fr.in2p3.jsaga.adaptor.naregi.security;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.globus.common.CoGProperties;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NaregiSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NaregiSecurityAdaptor extends UserPassSecurityAdaptor implements SecurityAdaptor {
    private boolean m_hasBackup;
    private String m_certRepositoryBackup;

    public NaregiSecurityAdaptor(String userId, String userPass, String certRepository) {
        super(userId, userPass);
        if (certRepository != null) {
            // save old cert repository
            m_hasBackup = true;
            m_certRepositoryBackup = CoGProperties.getDefault().getCaCertLocations();

            // set new cert repository
            CoGProperties.getDefault().setCaCertLocations(certRepository);
        }
    }

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.CERTREPOSITORY.equals(key)) {
            return CoGProperties.getDefault().getCaCertLocations();
        } else {
            return super.getAttribute(key);
        }
    }

    public void close() throws Exception {
        if (m_hasBackup) {
            // set old cert repository
            m_hasBackup = false;
            CoGProperties.getDefault().setCaCertLocations(m_certRepositoryBackup);
        }
    }

    public void dump(PrintStream out) throws Exception {
        out.println("  CertRepo : "+CoGProperties.getDefault().getCaCertLocations());
        super.dump(out);
    }
}
