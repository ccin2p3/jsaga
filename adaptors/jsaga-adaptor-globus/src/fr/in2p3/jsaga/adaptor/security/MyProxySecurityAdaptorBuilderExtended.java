package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.BadParameter;

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
public class MyProxySecurityAdaptorBuilderExtended extends MyProxySecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*12*3600;

    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"), new UFile("CertDir"),
            new U("Server"),
            new U("UserName"),
            new UHidden("MyProxyPass"),
/*
            new UOptional("UserName"),
            new UHidden("MyProxyPass") {
                public String toString() {return "[*"+m_name+"*]";}
                protected void throwExceptionIfInvalid(Object value) throws Exception {}
            },
*/
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
    });

    public Usage getInitUsage() {
        return CREATE_PROXY;
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        String userName = (String) attributes.get("UserName");
        String myProxyPass = (String) attributes.get("MyProxyPass");
        if (CREATE_PROXY.getMissingValues(attributes) == null) {
            GSSCredential cred = new GlobusProxyFactory(attributes).createProxy();
            int storedLifetime = attributes.containsKey("LifeTime")
                    ? UDuration.toInt(attributes.get("LifeTime"))
                    : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
            createMyProxy(attributes).put(cred, userName, myProxyPass, storedLifetime);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }
}
