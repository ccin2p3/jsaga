package fr.in2p3.jsaga.adaptor.language;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;

import java.io.InputStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LanguageAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface LanguageAdaptor {
    /**
     * @return the name of the language
     */
    public String getName();

    /**
     * Initialize the job description parser (expl: load language definition resources)
     */
    public void initParser() throws Exception;

    /**
     * Parse the job description
     * @param jobDescStream the job description
     * @return the job description as an XML document
     * @throws BadParameter if a syntax error occured
     * @throws NoSuccess if another error occured
     */
    public Document parseJobDescription(InputStream jobDescStream) throws BadParameter, NoSuccess;

    /**
     * @return the path to the stylesheet to transform the XML job description to JSDL
     */
    public String getTranslator();
}
