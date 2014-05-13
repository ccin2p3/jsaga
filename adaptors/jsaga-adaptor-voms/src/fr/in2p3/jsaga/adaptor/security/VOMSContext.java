package fr.in2p3.jsaga.adaptor.security;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSContext
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 fevr. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSContext {
    public static final String VOMSDIR = "VomsDir";
    public static final String VOMSES = "Vomses";
    public static final String USERFQAN = "UserFQAN";
    // TODO: use GlobusContext
    @Deprecated
    public static final String PROXYTYPE = "ProxyType";
    public static final String INITIALPROXY = "InitialUserProxy";
    public static final String USERPROXYSTRING = "UserProxyString";

    // MyProxy attributes
    public static final String MYPROXYSERVER = "MyProxyServer";
    public static final String MYPROXYUSERID = "MyProxyUserID";
}
