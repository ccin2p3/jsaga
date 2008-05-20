package fr.in2p3.jsaga.adaptor.language.abstracts;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.helpers.XMLFileParser;
import org.ogf.saga.error.BadParameter;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractLanguageAdaptorXML
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractLanguageAdaptorXML implements LanguageAdaptor {
    private XMLFileParser m_parser;

    protected void _initParser(String[] schemaResourcePaths) throws Exception {
        m_parser = new XMLFileParser(schemaResourcePaths);
    }

    public Document parseJobDescription(InputStream jobDescStream) throws BadParameter {
        try {
            File debugFile = new File(Base.JSAGA_VAR, "_parsed-job-description.xml");
            return m_parser.parse(jobDescStream, debugFile);
        } catch (Exception e) {
            throw new BadParameter(e);
        }
    }
}
