package fr.in2p3.jsaga.impl.buffer;

import org.ogf.saga.SagaObject;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ImplementationAllocatedBufferImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ImplementationAllocatedBufferImpl extends AbstractBufferImpl implements Buffer {
    /** constructor */
    public ImplementationAllocatedBufferImpl(int size) throws BadParameterException, NoSuccessException {
        super();
        this.setSize(size);
    }

    /** clone: copy buffer content */
    public SagaObject clone() throws CloneNotSupportedException {
        ImplementationAllocatedBufferImpl clone = (ImplementationAllocatedBufferImpl) super.clone();
        if (m_buffer != null) {
            clone.m_buffer = new byte[m_buffer.length];
            System.arraycopy(m_buffer, 0, clone.m_buffer, 0, m_buffer.length);
        } else {
            clone.m_buffer = null;
        }
        return clone;
    }

    public void setSize(int size) throws BadParameterException, NoSuccessException {
        if (size > -1) {
            m_buffer = new byte[size];
        } else {
            throw new BadParameterException("You must specify either the buffer or its size");
        }
    }

    public void setSize() throws BadParameterException, NoSuccessException {
        if (m_buffer != null) {
            m_buffer = new byte[m_buffer.length];
        } else {
            throw new BadParameterException("You must specify either the buffer or its size");
        }
    }

    public void setData(byte[] data) throws BadParameterException, IncorrectStateException, NoSuccessException {
        throw new IncorrectStateException("Not allowed to change the byte[] of an implementation-allocated buffer", this);
    }
}
