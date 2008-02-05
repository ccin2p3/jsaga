package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.globus.util.Util;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.context.Context;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityAdaptorBuilderExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityAdaptorBuilderExtended extends MyProxySecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*12*3600;

    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new UFilePath(Context.USERPROXY), new UFile(Context.USERCERT), new UFile(Context.USERKEY), new UHidden(Context.USERPASS), new UFile(Context.CERTREPOSITORY),
            new U(Context.SERVER),
            new U(Context.USERID),
            new UHidden(GlobusContext.MYPROXYPASS),
/*
            new UOptional(Context.USERID),
            new UHidden(GlobusContext.MYPROXYPASS) {
                public String toString() {return "[*"+m_name+"*]";}
                protected void throwExceptionIfInvalid(Object value) throws Exception {}
            },
*/
            new UDuration(Context.LIFETIME) {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
    });

    public Usage getInitUsage() {
        return CREATE_PROXY;
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        String userId = (String) attributes.get(Context.USERID);
        String myProxyPass = (String) attributes.get(GlobusContext.MYPROXYPASS);
        if (CREATE_PROXY.getMissingValues(attributes) == null) {
            GSSCredential cred = new GlobusProxyFactory(attributes, GlobusProxyFactory.OID_OLD).createProxy();
            int storedLifetime = attributes.containsKey(Context.LIFETIME)
                    ? UDuration.toInt(attributes.get(Context.LIFETIME))
                    : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
            createMyProxy(attributes).put(cred, userId, myProxyPass, storedLifetime);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    public void destroyBuilder(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
    }
}
