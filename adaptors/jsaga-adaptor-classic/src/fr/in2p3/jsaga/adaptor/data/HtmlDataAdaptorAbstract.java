package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.data.read.FileAttributes;
import fr.in2p3.jsaga.adaptor.data.read.FileReaderStreamFactory;
import org.ogf.saga.error.*;
import org.w3c.dom.*;
import org.xml.sax.SAXParseException;

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

            // convert to XML
            String xml = raw
                    .substring(raw.indexOf('\n')+1)
                    .replaceAll("<hr>","")
                    .replaceAll("> <a ","/> <a ");
            InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
            Element pre = (Element) doc.getElementsByTagName("pre").item(0);
            NodeList images = pre.getElementsByTagName("img");

            // convert to attributes list
            int nbEntries = images.getLength() - 2;
            FileAttributes[] list = new FileAttributes[nbEntries];
            for (int i=0; i<nbEntries; i++) {
                Element img = (Element) images.item(i+2);
                list[i] = new HtmlFileAttributes(img);
            }
            return list;
        } catch (SAXParseException e) {
            throw new PermissionDeniedException(e);
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
