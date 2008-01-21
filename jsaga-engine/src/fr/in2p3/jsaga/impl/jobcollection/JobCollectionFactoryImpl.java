package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.adaptor.language.LanguageAdaptor;
import fr.in2p3.jsaga.engine.factories.LanguageAdaptorFactory;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import fr.in2p3.jsaga.jobcollection.*;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;
import org.w3c.dom.Document;

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
    private LanguageAdaptorFactory m_adaptorFactory;

    public JobCollectionFactoryImpl(LanguageAdaptorFactory adaptorFactory) {
        m_adaptorFactory = adaptorFactory;
    }

    protected JobCollectionDescription doCreateJobCollectionDescription(String language, InputStream jobDescStream) throws NotImplemented, BadParameter, NoSuccess {
        LanguageAdaptor parser = m_adaptorFactory.getLanguageAdaptor(language);

        // parse
        Document jobDescDOM = parser.parseJobDescription(jobDescStream);

        // translate to JSDL
        String stylesheet = parser.getTranslator();
        Document jsdlDOM;
        if (stylesheet != null) {
            try {
                XSLTransformer transformer = XSLTransformerFactory.getInstance().getCached(stylesheet);
                jsdlDOM = transformer.transformToDOM(jobDescDOM);
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
        } else {
            jsdlDOM = jobDescDOM;
        }
        return new JobCollectionDescriptionImpl(jsdlDOM);
    }

    protected JobCollectionManager doCreateJobCollectionManager(Session session) throws NotImplemented, IncorrectURL, AuthenticationFailed, AuthorizationFailed, PermissionDenied, Timeout, NoSuccess {
        return new JobCollectionManagerImpl(session);
    }
}
