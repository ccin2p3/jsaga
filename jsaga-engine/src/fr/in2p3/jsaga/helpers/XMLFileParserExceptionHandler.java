package fr.in2p3.jsaga.helpers;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XMLFileParserExceptionHandler
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 avr. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XMLFileParserExceptionHandler extends DefaultHandler {
    private SAXParseException m_exception;

    public void error(SAXParseException exception) throws SAXException {
        m_exception = exception;
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        m_exception = exception;
    }

    public void warning(SAXParseException exception) throws SAXException {
        // ignore
    }

    public SAXParseException getSAXParseException() {
        return m_exception;
    }
}
