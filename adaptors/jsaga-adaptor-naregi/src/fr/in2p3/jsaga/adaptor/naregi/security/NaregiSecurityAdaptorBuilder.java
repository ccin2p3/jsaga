package fr.in2p3.jsaga.adaptor.naregi.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NaregiSecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 nov. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NaregiSecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    public String getType() {
        return "NAREGI";
    }

    public Class getSecurityAdaptorClass() {
        return NaregiSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new U(Context.USERID),
                new U(Context.USERPASS)
        });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        return new NaregiSecurityAdaptor(
                (String) attributes.get(Context.USERID),
                (String) attributes.get(Context.USERPASS),
                (String) attributes.get(Context.CERTREPOSITORY));
    }
}