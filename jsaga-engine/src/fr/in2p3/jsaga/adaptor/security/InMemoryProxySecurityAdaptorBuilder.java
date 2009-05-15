package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.UAnd;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.UNoPrompt;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.io.File;
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
        return new UAnd(new Usage[]{new UNoPrompt(Context.USERPROXY), new UFile(Context.CERTREPOSITORY)});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default(Context.CERTREPOSITORY, new File[]{
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")})
        };
    }

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        String base64 = (String) attributes.get(Context.USERPROXY);
        if (base64 != null) {
            File certRepository = new File((String) attributes.get(Context.CERTREPOSITORY));
            return new InMemoryProxySecurityAdaptor(base64, certRepository);
        } else {
            throw new NoSuccessException("Attribute is null: "+Context.USERPROXY);
        }
    }
}
