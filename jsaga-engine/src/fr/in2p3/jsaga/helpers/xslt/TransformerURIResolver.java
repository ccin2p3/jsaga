package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.engine.descriptors.AdaptorDescriptors;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   TransformerURIResolver
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class TransformerURIResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
        if ("SystemProperties.xml".equals(href)) {
            ByteArrayOutputStream xml = new ByteArrayOutputStream();
            try {
                System.getProperties().storeToXML(xml, "System properties");
                BufferedReader reader = new BufferedReader(new StringReader(xml.toString()));
                reader.readLine();
                reader.readLine();  // remove DTD (prevent from raising FileNotFoundException)
                return new StreamSource(reader);
            } catch (IOException e) {
                throw new TransformerException(e);
            }
        } else if ("AdaptorsDescriptor.xml".equals(href)) {
            try {
                byte[] xml = AdaptorDescriptors.getInstance().toByteArray();
                return new StreamSource(new ByteArrayInputStream(xml));
            } catch (Exception e) {
                throw new TransformerException(e);
            }
        } else if (href.startsWith("var/")) {
            return new StreamSource(new File(Base.JSAGA_VAR, href.substring(4)));
        } else {
            return new StreamSource(new File(Base.JSAGA_HOME, href));
        }
    }
}
