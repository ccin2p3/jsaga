package fr.in2p3.jsaga.impl.buffer;

import org.ogf.saga.SagaObject;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ApplicationAllocatedBufferImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ApplicationAllocatedBufferImpl extends AbstractBufferImpl implements Buffer {
    /** constructor */
    public ApplicationAllocatedBufferImpl(byte[] data) throws BadParameterException, NotImplementedException, NoSuccessException {
        super();
        this.setData(data);
    }

    /** clone: copy buffer reference */
    public SagaObject clone() throws CloneNotSupportedException {
        ApplicationAllocatedBufferImpl clone = (ApplicationAllocatedBufferImpl) super.clone();
        clone.m_buffer = m_buffer;
        return clone;
    }

    public void setSize(int size) throws NotImplementedException, BadParameterException, NoSuccessException {
        throw new NotImplementedException("Not allowed to change the size of an application-allocated buffer", this);
    }

    public void setSize() throws NotImplementedException, BadParameterException, NoSuccessException {
        throw new NotImplementedException("Not allowed to change the size of an application-allocated buffer", this);
    }

    public void setData(byte[] data) throws NotImplementedException, BadParameterException, NoSuccessException {
        if (data != null) {
            m_buffer = data;
        } else {
            throw new NotImplementedException("You must specify either the buffer or its size");
        }
    }
}
