package fr.in2p3.jsaga.adaptor.job.control.description;

import org.ogf.saga.error.NoSuccessException;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   JobDescriptionTranslatorXSLT
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   24 mars 2010
 * ***************************************************
 * Description:                                      */
/**
 * Use this translator if language supported by targeted scheduler is not JSDL
 */
public class JobDescriptionTranslatorXSLT implements JobDescriptionTranslator {
    private static final String UNIQID = "UniqId";

    private static Map s_stylesheetsMap = new HashMap();
    
    private Templates m_stylesheet;
    private Properties m_parameters;

    /**
     * Parse stylesheet
     * @param xslResourcePath the path to the stylesheet resource
     * @throws NoSuccessException if fails to parse the stylesheet
     */
    public JobDescriptionTranslatorXSLT(String xslResourcePath) throws NoSuccessException {
        // get stylesheet
        m_stylesheet = getStylesheet(xslResourcePath);

        // init parameters
        m_parameters = new Properties();
    }

    public void setAttribute(String key, String value) throws NoSuccessException {
        if (value != null) {
            m_parameters.setProperty(key, value);
        } else {
            throw new NoSuccessException("Service attribute is not set: "+key);
        }
    }

    public String translate(Document jsdl, String uniqId) throws NoSuccessException {
        // create transformer
        Transformer t;
        try {
            t = m_stylesheet.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new NoSuccessException("[ADAPTOR ERROR] Failed to parse stylesheet");
        }

        // set transformer
        TranslatorErrorListener listener = new TranslatorErrorListener();
        t.setErrorListener(listener);
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        t.setOutputProperty("{http://xml.apache.org/xalan/}indent-amount", "4");
        for (Iterator it=m_parameters.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            t.setParameter((String) entry.getKey(), entry.getValue());
        }
        if (uniqId != null) {
            t.setParameter(UNIQID, uniqId);
        }

        // transform
        StringWriter writer = new StringWriter();
        try {
            t.transform(new DOMSource(jsdl), new StreamResult(writer));
        } catch(TransformerException e) {
            // throw the cause of exception
            throw new NoSuccessException(listener.m_cause);
        }

        // return
        return writer.toString();
    }

    private static Templates getStylesheet(String xslResourcePath) throws NoSuccessException {
        if (s_stylesheetsMap.containsKey(xslResourcePath)) {
            // get from cache
            return (Templates) s_stylesheetsMap.get(xslResourcePath);
        } else {
            // load stylesheet
            InputStream stream = JobDescriptionTranslatorXSLT.class.getClassLoader().getResourceAsStream(xslResourcePath);
            if (stream == null) {
                throw new NoSuccessException("[ADAPTOR ERROR] Stylesheet not found: "+xslResourcePath);
            }

            // parse stylesheet
            Templates stylesheet;
            try {
                stylesheet = TransformerFactory.newInstance().newTemplates(new StreamSource(stream));
            } catch (TransformerConfigurationException e) {
                throw new NoSuccessException("[ADAPTOR ERROR] Failed to parse stylesheet: "+xslResourcePath);
            }

            // set to cache
            s_stylesheetsMap.put(xslResourcePath, stylesheet);

            // returns
            return stylesheet;
        }
    }

    class TranslatorErrorListener implements ErrorListener {
        private TransformerException m_cause;
        // invoked when <xsl:message terminate="yes"/>
        public void error(TransformerException exception) throws TransformerException {
            throw exception;
        }
        // invoked when <xsl:message terminate="no"/> or before fatal error
        public void warning(TransformerException exception) throws TransformerException {
            m_cause = exception;
            throw exception;
        }
        // explicitely invoked by XSLTransformer
        public void fatalError(TransformerException exception) throws TransformerException {
            throw m_cause;
        }
    }
}
