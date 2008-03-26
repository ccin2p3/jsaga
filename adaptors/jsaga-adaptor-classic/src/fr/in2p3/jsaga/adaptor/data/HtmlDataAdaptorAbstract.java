package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import org.ogf.saga.error.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

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

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // set base URL
        try {
            m_baseUrl = new URL(this.getType(), host, port, "/");
        } catch (MalformedURLException e) {
            throw new BadParameter(e);
        }
    }

    public void disconnect() throws NoSuccess {
        // unset base URL
        m_baseUrl = null;
    }

    public boolean isEntry(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        return !this.isDirectory(absolutePath, additionalArgs);
    }

    public FileAttributes[] listAttributes(String absolutePath, String additionalArgs) throws PermissionDenied, DoesNotExist, Timeout, NoSuccess {
        try {
            // get index.html
            InputStream rawStream = this.getInputStream(absolutePath, additionalArgs);
            InputStream xmlStream = new ByteArrayInputStream(
                    toString(rawStream).replaceAll("<hr>","").replaceAll("> <a ","/> <a ").getBytes());
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
            Element pre = (Element) doc.getElementsByTagName("pre").item(0);
            NodeList images = pre.getElementsByTagName("img");

            // convert to attributes list
            int nbEntries = images.getLength() - 2;
            FileAttributes[] list = new FileAttributes[nbEntries];
            for (int i=0; i<nbEntries; i++) {
                Element img = (Element) images.item(i+2);
                list[i] = new HttpFileAttributes(img);
            }
            return list;
        } catch (BadParameter e) {
            throw new NoSuccess(e);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
    private String toString(InputStream in) throws IOException {
        int len;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.skip("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">".length());
        while ((len=in.read(buffer)) > -1) {
            bytes.write(buffer, 0, len);
        }
        return bytes.toString();
    }
}
