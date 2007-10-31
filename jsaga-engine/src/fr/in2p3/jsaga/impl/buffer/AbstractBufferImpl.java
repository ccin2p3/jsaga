package fr.in2p3.jsaga.impl.buffer;

import fr.in2p3.jsaga.impl.AbstractSagaObjectImpl;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaObject;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractBufferImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractBufferImpl extends AbstractSagaObjectImpl implements Buffer {
    protected byte[] m_buffer;

    /** constructor */
    public AbstractBufferImpl() {
        m_buffer = null;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        // do not assign m_buffer here (see inherited classes)
        return super.clone();
    }

    public ObjectType getType() {
        return ObjectType.BUFFER;
    }

    public int getSize() throws NotImplemented, IncorrectState {
        if (m_buffer != null) {
            return m_buffer.length;
        } else {
            return -1;
        }
    }

    public byte[] getData() throws NotImplemented, DoesNotExist, IncorrectState {
        if (m_buffer != null) {
            return m_buffer;
        } else {
            throw new DoesNotExist("No I/O operation has been done on this buffer yet");
        }
    }

    public void close() throws NotImplemented, IncorrectState {
        m_buffer = null;
    }

    public void close(float timeoutInSeconds) throws NotImplemented, IncorrectState {
        this.close();
    }
}
