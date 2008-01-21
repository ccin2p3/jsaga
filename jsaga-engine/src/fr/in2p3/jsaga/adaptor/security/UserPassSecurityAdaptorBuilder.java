package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectState;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   UserPassSecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class UserPassSecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    protected static final Usage UNCRYPTED = new UAnd(new Usage[]{new U(Context.USERID), new UHidden(Context.USERPASS)});

    public String getType() {
        return "UserPass";
    }

    public Class getSecurityAdaptorClass() {
        return UserPassSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return UNCRYPTED;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{
                new Default(Context.USERID, "anonymous"),
                new Default(Context.USERPASS, "anon")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        return new UserPassSecurityAdaptor(
                (String) attributes.get(Context.USERID),
                (String) attributes.get(Context.USERPASS));
    }
}
