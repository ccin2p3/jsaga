package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UNoPrompt;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   InMemoryProxySecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class InMemoryProxySecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    public String getType() {
        return "InMemoryProxy";
    }

    public Class getSecurityAdaptorClass() {
        return GSSCredentialSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UNoPrompt("UserProxy");
//        return new UAnd(new Usage[]{new UNoPrompt("UserProxy"), new UFile("CertDir")});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
/*
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default("CertDir", new File[]{
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")})
        };
*/
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        String base64 = (String) attributes.get("UserProxy");
        if (base64 != null) {
            return new InMemoryProxySecurityAdaptor(base64);
        } else {
            throw new BadParameter("Missing attribute: UserProxy");
        }
    }
}
