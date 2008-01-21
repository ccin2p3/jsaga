package fr.in2p3.jsaga.impl.jobcollection;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import fr.in2p3.jsaga.jobcollection.JobCollectionDescription;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccess;
import org.w3c.dom.Document;

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
    private Document m_jsdl;

    /** constructor */
    public JobCollectionDescriptionImpl(Document jsdl) throws NoSuccess {
        super();
        m_jsdl = jsdl;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        JobCollectionDescriptionImpl clone = (JobCollectionDescriptionImpl) super.clone();
        clone.m_jsdl = m_jsdl;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.UNKNOWN;
    }

    public Document getJSDL() {
        return m_jsdl;
    }
}
