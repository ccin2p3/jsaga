package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.UFile;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.context.Context;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   X509SecurityAdaptorBuilder
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class X509SecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    public String getType() {
        return "X509";
    }

    public Class getSecurityAdaptorClass() {
        return X509SecurityAdaptor.class;
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        return new X509SecurityAdaptor();
    }

    public Usage getUsage() {
        return new UFile(Context.USERCERT);
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }
}
