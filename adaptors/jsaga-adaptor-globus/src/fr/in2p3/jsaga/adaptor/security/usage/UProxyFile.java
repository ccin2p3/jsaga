package fr.in2p3.jsaga.adaptor.security.usage;

import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.IncorrectStateException;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UProxyFile
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UProxyFile extends UFile {
    private int m_minLifeTime;

    public UProxyFile(int id, String name, int minLifeTime) {
        super(id, name);
        m_minLifeTime = minLifeTime;
    }

    public String toString() {
        return "<"+m_name+":"+m_minLifeTime+">";
    }

    protected Object throwExceptionIfInvalid(Object value) throws Exception {
        File file = (File) super.throwExceptionIfInvalid(value);
        GSSCredential cred = load(file);
        if (cred.getRemainingLifetime() < m_minLifeTime) {
            throw new IncorrectStateException("Proxy file remaining lifetime is not enougth: "+cred.getRemainingLifetime());
        }
        return cred;
    }

    private static GSSCredential load(File proxyFile) throws IOException, GSSException {
        byte [] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(
                proxyBytes,
                ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME,
                null, // use default mechanism: GSI
                GSSCredential.ACCEPT_ONLY);
    }
}
