package fr.in2p3.jsaga.impl.buffer;

import org.ogf.saga.SagaBase;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NotImplemented;

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
    public ApplicationAllocatedBufferImpl(byte[] data) throws BadParameter, NotImplemented {
        super();
        this.setData(data);
    }

    /** constructor for deepCopy: copy buffer reference */
    public ApplicationAllocatedBufferImpl(ApplicationAllocatedBufferImpl source) {
        m_buffer = source.m_buffer;
    }
    public SagaBase deepCopy() {
        return new ApplicationAllocatedBufferImpl(this);
    }

    public void setSize(int size) throws NotImplemented, BadParameter {
        throw new NotImplemented("Not allowed to change the size of an application-allocated buffer", this);
    }

    public void setData(byte[] data) throws NotImplemented, BadParameter {
        if (data != null) {
            m_buffer = data;
        } else {
            throw new NotImplemented("You must specify either the buffer or its size");
        }
    }
}
