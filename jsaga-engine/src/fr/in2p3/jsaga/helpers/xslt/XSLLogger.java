package fr.in2p3.jsaga.helpers.xslt;

import org.apache.log4j.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XSLLogger
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 déc. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XSLLogger implements ErrorListener {
    private static Logger s_logger = Logger.getLogger(XSLLogger.class);
    private TransformerException m_cause;

    /** invoked when <xsl:message terminate="no"/> */
    public void warning(TransformerException exception) throws TransformerException {
        m_cause = exception;
        s_logger.info(exception.getMessageAndLocation());
        throw exception;
    }

    /** invoked when <xsl:message terminate="yes"/> */
    public void error(TransformerException exception) throws TransformerException {
        s_logger.error(exception.getMessageAndLocation());
        throw exception;
    }

    /** explicitely invoked by XSLTransformer */
    public void fatalError(TransformerException exception) throws TransformerException {
        if (m_cause != null) {
            throw m_cause;
        } else {
            throw exception;
        }
    }
}
