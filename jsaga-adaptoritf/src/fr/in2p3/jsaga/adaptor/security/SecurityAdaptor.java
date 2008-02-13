package fr.in2p3.jsaga.adaptor.security;

import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SecurityAdaptor {
    public static final int INFINITE_LIFETIME = -1;

    /**
     * @return the identifier of the user.
     */
    public String getUserID() throws Exception;

    /**
     * @return the value of the attribute (other than UserID)
     * @throws NotImplemented if the attribute <code>key</code> is not supported by this adaptor
     * @throws NoSuccess if the adaptor failed to get the value of attribute <code>key</code>
     */
    public String getAttribute(String key) throws NotImplemented, NoSuccess;

    /**
     * Close the context (implementation may be empty).
     */
    public void close() throws Exception;

    /**
     * @return description of security context instance.
     */
    public void dump(PrintStream out) throws Exception;
}
