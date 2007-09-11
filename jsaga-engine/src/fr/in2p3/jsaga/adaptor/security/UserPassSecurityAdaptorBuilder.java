package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NotImplemented;

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
    public String getType() {
        return "UserPass";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{new U("UserName"), new UHidden("UserPass")});
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return new Default[]{
                new Default("UserName", "anonymous"),
                new Default("UserPass", "anon")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws NotImplemented {
        return new UserPassSecurityAdaptor(
                (String) attributes.get("UserName"),
                (String) attributes.get("UserPass"));
    }
}
