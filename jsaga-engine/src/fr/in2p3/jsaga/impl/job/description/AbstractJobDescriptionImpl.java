package fr.in2p3.jsaga.impl.job.description;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.job.JobDescription;
import org.w3c.dom.Element;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractJobDescriptionImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractJobDescriptionImpl extends AbstractAttributesImpl implements JobDescription {
    private Element m_jsdl;

    /** constructor */
    public AbstractJobDescriptionImpl(Element jobDescDOM) {
        super(null, true);
        m_jsdl = jobDescDOM;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        AbstractJobDescriptionImpl clone = (AbstractJobDescriptionImpl) super.clone();
        clone.m_jsdl = (Element) m_jsdl.cloneNode(true);
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.JOBDESCRIPTION;
    }

    public Element getJSDL() throws NoSuccess {
        return m_jsdl;
    }
}
