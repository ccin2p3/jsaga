package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XSLTransformerFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   6 mai 2007
* ***************************************************
* Description:                                      */
/**
 * A thread-safe factory of Transformer, which caches the parsing of stylesheets
 */
public class XSLTransformerFactory {
    private static XSLTransformerFactory _instance;

    private TransformerFactory m_factory;
    private Map m_templatesMap;
    private File m_debugBaseDir;

    public static XSLTransformerFactory getInstance() {
        if (_instance == null) {
            _instance = new XSLTransformerFactory();
        }
        return _instance;
    }
    private XSLTransformerFactory() {
        m_factory = TransformerFactory.newInstance();
        if ("net.sf.saxon.TransformerFactoryImpl".equals(m_factory.getClass().getName())) {
            // force using XALAN because SAXON is too restrictive for stylesheet jsaga-default-contexts-merge.xsl
            System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
            m_factory = TransformerFactory.newInstance();
        }
        m_templatesMap = new HashMap();
        m_debugBaseDir = new File(Base.JSAGA_VAR, "debug");
        if (Base.DEBUG && !m_debugBaseDir.exists()) {
            m_debugBaseDir.mkdir();
        }
    }

    public XSLTransformer getCached(String xslFile) throws TransformerConfigurationException {
        return getCached(xslFile, null, null);
    }
    public XSLTransformer getCached(String xslFile, URIResolver uriResolver) throws TransformerConfigurationException {
        return getCached(xslFile, uriResolver, null);
    }
    public XSLTransformer getCached(String xslFile, Map parameters) throws TransformerConfigurationException {
        return getCached(xslFile, null, parameters);
    }
    public XSLTransformer getCached(String xslFile, URIResolver uriResolver, Map parameters) throws TransformerConfigurationException {
        Templates templates = (Templates) m_templatesMap.get(xslFile);
        if (templates == null) {
            templates = m_factory.newTemplates(this.getXSLSource(xslFile));
            m_templatesMap.put(xslFile, templates);
        }
        return getXSLTransformer(
                xslFile,
                templates.newTransformer(),
                (uriResolver!=null ? uriResolver : new TransformerURIResolver()),
                (parameters!=null ? parameters : new HashMap()));
    }

    public XSLTransformer create(String xslFile) throws TransformerConfigurationException {
        return create(xslFile, null, null);
    }
    public XSLTransformer create(String xslFile, URIResolver uriResolver) throws TransformerConfigurationException {
        return create(xslFile, uriResolver, null);
    }
    public XSLTransformer create(String xslFile, Map parameters) throws TransformerConfigurationException {
        return create(xslFile, null, parameters);
    }
    public XSLTransformer create(String xslFile, URIResolver uriResolver, Map parameters) throws TransformerConfigurationException {
        return getXSLTransformer(
                xslFile,
                m_factory.newTransformer(this.getXSLSource(xslFile)),
                (uriResolver!=null ? uriResolver : new TransformerURIResolver()),
                (parameters!=null ? parameters : new HashMap()));
    }

    private Source getXSLSource(String xslFile) throws TransformerConfigurationException {
        InputStream stream = XSLTransformerFactory.class.getClassLoader().getResourceAsStream(xslFile);
        if (stream != null) {
            return new StreamSource(stream);
        } else {
            throw new TransformerConfigurationException("Stylesheet not found: "+xslFile);
        }
    }

    private XSLTransformer getXSLTransformer(String xslFile, Transformer transformer, URIResolver uriResolver, Map parameters) {
        // set transformer
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setURIResolver(uriResolver);
        transformer.setErrorListener(new XSLLogger());
        for (Iterator it=EngineProperties.getProperties().entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            transformer.setParameter((String) entry.getKey(), entry.getValue());
        }
        for (Iterator it=parameters.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            transformer.setParameter((String) entry.getKey(), entry.getValue());
        }
        // set debug file
        String debugFileName = new File(xslFile).getName()+".xml";
        File debugFile = new File(m_debugBaseDir, debugFileName);
        // encapsulate
        return new XSLTransformer(transformer, debugFile);
    }
}
