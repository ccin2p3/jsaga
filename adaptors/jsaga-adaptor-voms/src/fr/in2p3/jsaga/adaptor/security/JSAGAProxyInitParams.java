package fr.in2p3.jsaga.adaptor.security;

import org.bouncycastle.openssl.PasswordFinder;
import org.italiangrid.voms.clients.ProxyInitParams;

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

    private String userPass;
    
    public void setUserPass(String p){
        this.userPass = p;
    }
    
    public String getUserPass() {
        return this.userPass;
    }
    
    public PasswordFinder getPasswordFinder() {
        if (this.userPass != null) {
            return new PasswordFinder() {

                public char[] getPassword() {
                    return userPass.toCharArray();
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
