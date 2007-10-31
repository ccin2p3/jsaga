package fr.in2p3.jsaga.adaptor.security;

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
    public static final int INFINITE = -1;

    /**
     * @return the identifier of the user.
     */
    public String getUserID() throws Exception;

    /**
     * @return the time left or <code>INFINITE</code>.
     */
    public int getTimeLeft() throws Exception;

    /**
     * Close the context (implementation may be empty).
     */
    public void close() throws Exception;

    /**
     * @return description of security context instance.
     */
    public void dump(PrintStream out) throws Exception;
}
