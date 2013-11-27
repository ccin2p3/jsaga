package fr.in2p3.jsaga.adaptor.security;

import java.util.Map;

import org.bouncycastle.openssl.PasswordFinder;
import org.italiangrid.voms.clients.ProxyInitParams;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JSAGAProxyInitParams
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description: a ProxyInitParams with the user password */
public class JSAGAProxyInitParams extends ProxyInitParams {

//    private String userPass;
//    private String server;
    private Map m_attributes;
    
    public void setContext(Map a) {
        this.m_attributes = a;
    }
    
    @Deprecated
    public void setUserPass(String p){
//        this.userPass = p;
    }
    
    public String getUserPass() {
//        return this.userPass;
        return ((String)m_attributes.get(Context.USERPASS));
    }
    
    @Deprecated
    public void setServer(String s) {
//        this.server = s;
    }

    public String getServer() {
        return ((String)m_attributes.get(Context.SERVER));
    }
    
    public String getVOName() {
        return ((String)m_attributes.get(Context.USERVO));
    }
    
    public PasswordFinder getPasswordFinder() {
        if (this.getUserPass() != null) {
            return new PasswordFinder() {

                public char[] getPassword() {
                    return getUserPass().toCharArray();
                }
                
            };
        }
        return null;
    }

    @Override
    public boolean isReadPasswordFromStdin() {
        return true;
    }

    @Override
    public void setReadPasswordFromStdin(boolean readPasswordFromStdin) {
    }


}
