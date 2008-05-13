package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.helpers.xpath.XJSDLXPathSelector;
import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.jobcollection.JobCollectionDescription;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionDescriptionImpl extends AbstractSagaObjectImpl implements JobCollectionDescription {
    private String m_collectionName;
    private Document m_document;

    /** constructor */
    public JobCollectionDescriptionImpl(Document jcDesc, String collectionName) throws NoSuccess {
        super();
        XJSDLXPathSelector selector = new XJSDLXPathSelector(jcDesc);
        Element elem = (Element) selector.getNode("/ext:JobCollection/ext:JobCollectionDescription/ext:JobCollectionIdentification/ext:JobCollectionName");
        if (elem == null) {
            throw new NoSuccess("Language adaptor must create a default JobCollectionName element");
        }
        if (collectionName != null) {
            elem.setTextContent(collectionName);
            m_collectionName = collectionName;
        } else {
            m_collectionName = elem.getTextContent();
        }
        m_document = jcDesc;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionDescriptionImpl clone = (JobCollectionDescriptionImpl) super.clone();
        clone.m_collectionName = m_collectionName;
        clone.m_document = m_document;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.UNKNOWN;
    }

    public String getCollectionName() {
        return m_collectionName;
    }

    public Document getAsDocument() {
        return m_document;
    }
}
