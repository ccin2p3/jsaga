package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.ogf.saga.error.BadParameter;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorBuilderExtended
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusSecurityAdaptorBuilderExtended extends GlobusSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"), new UFile("CertDir"),
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
            new UOptional("Delegation") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                            throw new BadParameter("Expected: limited | full");
                        }
                    }
                    return value;
                }
            },
            new UOptional("ProxyType") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC820")) {
                            throw new BadParameter("Expected: old | globus | RFC820");
                        }
                    }
                    return value;
                }
            }
    });

    public Usage getInitUsage() {
        return CREATE_PROXY;
    }

    public void initBuilder(Map attributes, String contextId) throws Exception {
        new GlobusProxyFactory(attributes).createProxy();
    }
}
