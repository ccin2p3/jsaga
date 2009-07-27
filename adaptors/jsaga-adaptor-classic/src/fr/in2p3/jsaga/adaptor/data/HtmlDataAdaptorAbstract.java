package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   HtmlDataAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class HtmlDataAdaptorAbstract implements FileReaderStreamFactory {
    protected URL m_baseUrl;

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // set base URL
        try {
            m_baseUrl = new URL(this.getType(), host, port, "/");
        } catch (MalformedURLException e) {
            throw new BadParameterException(e);
        }
    }

    public void disconnect() throws NoSuccessException {
        // unset base URL
        m_baseUrl = null;
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDeniedException, DoesNotExistException, TimeoutException, NoSuccessException {
        try {
            // get web page
            InputStream rawStream = this.getInputStream(absolutePath, additionalArgs);
            String raw = toString(rawStream);
            if (raw.contains("403 Forbidden")) {
                throw new PermissionDeniedException("Not allowed to list this directory");
            }

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
