package fr.in2p3.jsaga.impl.buffer;

import org.ogf.saga.SagaBase;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NotImplemented;

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
    public ImplementationAllocatedBufferImpl(int size) throws BadParameter, NotImplemented {
        super();
        this.setSize(size);
    }

    /** constructor for deepCopy: copy buffer content */
    public ImplementationAllocatedBufferImpl(ImplementationAllocatedBufferImpl source) {
        if (source.m_buffer != null) {
            m_buffer = new byte[source.m_buffer.length];
            System.arraycopy(source.m_buffer, 0, m_buffer, 0, m_buffer.length);
        } else {
            m_buffer = null;
        }
    }
    public SagaBase deepCopy() {
        return new ImplementationAllocatedBufferImpl(this);
    }

    public void setSize(int size) throws NotImplemented, BadParameter {
        if (size > -1) {
            m_buffer = new byte[size];
        } else {
            throw new NotImplemented("You must specify either the buffer or its size");
        }
    }

    public void setData(byte[] data) throws NotImplemented, BadParameter {
        throw new NotImplemented("Not allowed to change the byte[] of an implementation-allocated buffer", this);
    }
}
