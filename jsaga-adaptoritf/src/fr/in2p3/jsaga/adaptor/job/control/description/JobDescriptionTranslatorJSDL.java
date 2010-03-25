package fr.in2p3.jsaga.adaptor.job.control.description;

import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobDescriptionTranslatorJSDL
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   24 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 * Use this translator if language supported by targeted scheduler is JSDL
 */
public class JobDescriptionTranslatorJSDL implements JobDescriptionTranslator {
    /**
     * Do nothing
     */
    public void setAttribute(String key, String value) {
        // do nothing
    }

    /**
     * Serialize JSDL document to string
     */
    public String translate(Document jsdl, String uniqId) throws NoSuccessException {
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty("{http://xml.apache.org/xalan/}indent-amount", "4");
            transformer.transform(new DOMSource(jsdl), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new NoSuccessException(e);
        }
        return writer.toString();
    }
}
