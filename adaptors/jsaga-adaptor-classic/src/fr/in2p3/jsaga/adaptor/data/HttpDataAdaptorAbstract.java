package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ogf.saga.error.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
    
    public String getNativeScheme() {
        return "http";
    }
    
    public Usage getUsage() {
        return null;    // no usage
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;    // no default
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class, null}; // also support no security context
    }

    public void setSecurityCredential(SecurityCredential credential) {
        if (credential!= null) {
            UserPassSecurityCredential adaptor = (UserPassSecurityCredential) credential;
            m_userID = adaptor.getUserID();
            m_userPass = adaptor.getUserPass();
        }
    }

    public int getDefaultPort() {
        return 80;
    }

    public abstract boolean exists(String absolutePath, String additionalArgs) throws PermissionDeniedException, TimeoutException, NoSuccessException;
    public abstract FileAttributes getAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException;
    public abstract InputStream getInputStream(String absolutePath, String additionalArgs) throws PermissionDeniedException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException;

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            // get web page
            InputStream rawStream = this.getInputStream(absolutePath, additionalArgs);
            String raw = toString(rawStream);
            // LS commented this as this exception is already sent by getInputStream()
//            if (raw.contains("403 Forbidden")) {
//                throw new PermissionDeniedException("Not allowed to list this directory");
//            }

            // extract href
            List list = new ArrayList();
            String[] array = raw.split("href=\"");
            for (int i=1; i<array.length; i++) {
                String entryName = array[i].substring(0, array[i].indexOf('"'));
                boolean isDir = false;
                while (entryName.endsWith("/")) {
                    isDir = true;
                    entryName = entryName.substring(0, entryName.length() - 1);
                }
                if (!entryName.contains("/") && !entryName.contains("?") && !entryName.contains("#")) {
                	entryName = java.net.URLDecoder.decode(entryName, "utf-8");
                	entryName = new java.io.File(entryName).getPath();
                    list.add(new HtmlFileAttributes(entryName, isDir));
                }
            }
            return (FileAttributes[]) list.toArray(new FileAttributes[list.size()]);
        } catch (BadParameterException e) {
            throw new NoSuccessException(e);
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    private String toString(InputStream in) throws IOException {
        int len;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while ((len=in.read(buffer)) > -1) {
            bytes.write(buffer, 0, len);
        }
        return bytes.toString();
    }

}
