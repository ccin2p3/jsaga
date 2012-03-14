package fr.in2p3.jsaga.helpers;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.StringWriter;

/**
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DOMRenderer
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 */
/**
 * For debugging purpose
 */
public class DOMRenderer {
    public static String toString(Document dom) throws IOException {
        StringWriter writer = new StringWriter();
        com.sun.org.apache.xml.internal.serialize.OutputFormat format = new com.sun.org.apache.xml.internal.serialize.OutputFormat(dom);
        format.setIndenting(true);
        format.setLineSeparator(System.getProperty("line.separator"));
        new XMLSerializer(writer, format).asDOMSerializer().serialize(dom);
        return writer.toString();
    }
}
