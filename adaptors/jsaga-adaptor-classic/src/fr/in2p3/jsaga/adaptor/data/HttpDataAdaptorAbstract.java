package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.InputStream;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HttpDataAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class HttpDataAdaptorAbstract extends HtmlDataAdaptorAbstract implements DataAdaptor {
    protected String m_userID;
    protected String m_userPass;

    public String getType() {
        return "http";
    }

    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;    // no default
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{UserPassSecurityAdaptor.class, null}; // also support no security context
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        if (securityAdaptor != null) {
            UserPassSecurityAdaptor adaptor = (UserPassSecurityAdaptor) securityAdaptor;
            m_userID = adaptor.getUserID();
            m_userPass = adaptor.getUserPass();
        }
    }

    public BaseURL getBaseURL() throws IncorrectURL {
        return new BaseURL(80);
    }

    public abstract boolean exists(String absolutePath, String additionalArgs) throws PermissionDenied, Timeout, NoSuccess;
    public abstract FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess;
    public abstract InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDenied, BadParameter, DoesNotExist, Timeout, NoSuccess;
}
