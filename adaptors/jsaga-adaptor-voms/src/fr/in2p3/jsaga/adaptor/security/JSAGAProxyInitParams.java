package fr.in2p3.jsaga.adaptor.security;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import fr.in2p3.jsaga.adaptor.base.usage.UDuration;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JSAGAProxyInitParams
 * Author: lionel.schwarz@in2p3.fr
 * Date:   27 nov 2013
 * ***************************************************
 * Description: a ProxyInitParams with some more params */
public class JSAGAProxyInitParams extends ProxyInitParams {

    private Map m_attributes;
    
    public JSAGAProxyInitParams(Map attributes) {
        super();
        this.m_attributes = attributes;
        
        setGeneratedProxyFile((String)attributes.get(Context.USERPROXY));
        setProxyType(ProxyTypeMap.toProxyType((String)attributes.get(VOMSContext.PROXYTYPE)));
        setVomsdir((String) attributes.get(VOMSContext.VOMSDIR));
        setTrustAnchorsDir((String) attributes.get(Context.CERTREPOSITORY));
        
        setVomsCommands(Arrays.asList((String) attributes.get(Context.USERVO)));
        if (attributes.containsKey(VOMSContext.VOMSES)) {
            setVomsesLocations(Arrays.asList((String) attributes.get(VOMSContext.VOMSES)));
        }
        
        // TODO: test this
        if (attributes.containsKey(VOMSContext.USERFQAN)) {
            List<String> fqans = new ArrayList<String>();
            fqans.add((String) attributes.get(VOMSContext.USERFQAN));
            setFqanOrder(fqans);
        }
        
        try {
            int sec = UDuration.toInt(attributes.get(Context.LIFETIME));
            setProxyLifetimeInSeconds(sec);
            setAcLifetimeInSeconds(sec);
        } catch (ParseException e) {
        }

        setLimited(DelegationTypeMap.toLimitedValue((String)attributes.get(VOMSContext.DELEGATION)));
        
    }
    
    public String getUserPass() {
        return ((String)m_attributes.get(Context.USERPASS));
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
