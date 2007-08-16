package fr.in2p3.jsaga.helpers.xslt;

import fr.in2p3.jsaga.Base;

import javax.xml.transform.*;
import java.io.File;

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
 *
 */
public class XSLTransformerFactory {
    private static TransformerCachedFactory s_cachedFactory = new TransformerCachedFactory();
    private String m_xslBaseDir;
    private File m_debugBaseDir;

    public XSLTransformerFactory(String xslBaseDir, File baseDir) {
        m_xslBaseDir = xslBaseDir;
        m_debugBaseDir = new File(baseDir, "debug");
        if (Base.DEBUG && !m_debugBaseDir.exists()) {
            m_debugBaseDir.mkdir();
        }
    }

    public XSLTransformer getCached(String xslFile) throws TransformerConfigurationException {
        return new XSLTransformer(
                s_cachedFactory.getCached(m_xslBaseDir, xslFile),
                new File(m_debugBaseDir, xslFile+".xml"));
    }

    public XSLTransformer create(String xslFile) throws TransformerConfigurationException {
        return new XSLTransformer(
                s_cachedFactory.create(m_xslBaseDir, xslFile),
                new File(m_debugBaseDir, xslFile+".xml"));
    }

    public XSLTransformer create(String xslFile, URIResolver uriResolver) throws TransformerConfigurationException {
        Transformer transformer = s_cachedFactory.create(m_xslBaseDir, xslFile);
        transformer.setURIResolver(uriResolver);
        return new XSLTransformer(
                transformer,
                new File(m_debugBaseDir, xslFile+".xml"));
    }
}
