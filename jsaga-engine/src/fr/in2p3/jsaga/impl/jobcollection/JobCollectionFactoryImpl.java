package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.factories.EvaluatorAdaptorFactory;
import fr.in2p3.jsaga.engine.factories.LanguageAdaptorFactory;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.jobcollection.*;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.lang.Exception;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionFactoryImpl extends JobCollectionFactory {
    private LanguageAdaptorFactory m_languageFactory;
    private EvaluatorAdaptorFactory m_evaluatorFactory;

    public JobCollectionFactoryImpl(LanguageAdaptorFactory languageFactory, EvaluatorAdaptorFactory evaluatorFactory) {
        m_languageFactory = languageFactory;
        m_evaluatorFactory = evaluatorFactory;
    }

    protected JobCollectionDescription doCreateJobCollectionDescription(String language, InputStream jobDescStream) throws NotImplemented, BadParameter, NoSuccess {
        LanguageAdaptor parser = m_languageFactory.getLanguageAdaptor(language);

        // parse
        Document jobDescDOM = parser.parseJobDescription(jobDescStream);

        // debug
        if (Base.DEBUG) {
            File debugFile = new File(new File(Base.JSAGA_VAR, "debug"), parser.getName()+".xml");
            try {
                TransformerFactory.newInstance().newTransformer().transform(
                        new DOMSource(jobDescDOM), new StreamResult(debugFile));
            } catch (TransformerException e) {}
        }

        // translate to JSDL
        String stylesheet = parser.getTranslator();
        if (stylesheet == null) {
            throw new NotImplemented("[ADAPTOR ERROR] Method getTranslator() must not return 'null'");
        }
        Document jcDesc;
        try {
            XSLTransformer transformer = XSLTransformerFactory.getInstance().getCached(stylesheet);
            jcDesc = transformer.transformToDOM(jobDescDOM);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return new JobCollectionDescriptionImpl(jcDesc);
    }

    protected JobCollectionManager doCreateJobCollectionManager(Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return new JobCollectionManagerImpl(session, m_evaluatorFactory.getEvaluatorAdaptor());
    }
}
