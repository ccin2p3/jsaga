package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.globus.util.Util;
import org.ogf.saga.error.BadParameter;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorBuilderExtendedAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GlobusSecurityAdaptorBuilderExtendedAbstract extends GlobusSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
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
            }
    });

    public Usage getInitUsage() {
        return CREATE_PROXY;
    }

    public abstract void initBuilder(Map attributes, String contextId) throws Exception;

    public void destroyBuilder(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get("UserProxy");
        Util.destroy(proxyFile);
    }
}
