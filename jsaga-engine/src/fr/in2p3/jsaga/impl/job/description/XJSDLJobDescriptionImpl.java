package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.helpers.xslt.XSLTransformer;
import fr.in2p3.jsaga.helpers.xslt.XSLTransformerFactory;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccess;
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
    private Document m_document;

    /** constructor */
    public XJSDLJobDescriptionImpl(Document document) throws NoSuccess {
        super();

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
            throw new NoSuccess(e);
        }
        for (Iterator it=prop.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            super._addReadOnlyAttribute(name, value);
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        XJSDLJobDescriptionImpl clone = (XJSDLJobDescriptionImpl) super.clone();
        clone.m_document = (Document) m_document.cloneNode(true);
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.JOBDESCRIPTION;
    }

    public Document getAsDocument() throws NoSuccess {
        return m_document;
    }
}
