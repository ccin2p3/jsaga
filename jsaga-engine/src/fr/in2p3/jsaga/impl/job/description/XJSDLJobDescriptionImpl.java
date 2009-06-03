package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.adaptor.language.SAGALanguageAdaptor;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   XJSDLJobDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 mars 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class XJSDLJobDescriptionImpl extends AbstractJobDescriptionImpl implements JobDescription {
    private static final String SAGA_JOB_DESCRIPTION = "xsl/saga-job-description.xsl";
    private String m_collectionName;
    private String m_jobName;
    private Document m_document;

    /** constructor */
    public XJSDLJobDescriptionImpl(String collectionName, String jobName, Document document) throws NoSuccessException {
        super();

        // set job collection name
        m_collectionName = collectionName;

        // set job name
        m_jobName = jobName;

        // set job description document
        m_document = document;

        // set SAGA attributes
        Properties prop;
        try {
            XSLTransformer transformer = XSLTransformerFactory.getInstance().getCached(SAGA_JOB_DESCRIPTION);
            byte[] propBytes = transformer.transform(document.getDocumentElement());
            prop = new Properties();
            prop.load(new ByteArrayInputStream(propBytes));
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        SAGALanguageAdaptor sagaProperties = new SAGALanguageAdaptor();
        try {
            sagaProperties.initParser();
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (sagaProperties.isVectoryProperty(name)) {
                super._addVectorAttribute(name, value);
            } else {
                super._addAttribute(name, value);
            }
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        XJSDLJobDescriptionImpl clone = (XJSDLJobDescriptionImpl) super.clone();
        clone.m_document = (Document) m_document.cloneNode(true);
        return clone;
    }

    public String getCollectionName() {
        return m_collectionName;
    }

    public String getJobName() {
        return m_jobName;
    }

    public Document getAsDocument() {
        return m_document;
    }
}
