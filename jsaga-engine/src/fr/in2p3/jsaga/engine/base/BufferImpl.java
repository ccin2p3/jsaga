package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BufferImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class BufferImpl extends AbstractSagaBaseImpl implements Buffer {
    protected byte[] m_data;

    /** constructor */
    public BufferImpl(byte[] data) {
        super();
        m_data = data;
    }

    /** constructor for deepCopy */
    public BufferImpl(BufferImpl source) {
        super(source);
        if (source.m_data != null) {
            m_data = new byte[source.m_data.length];
            System.arraycopy(source.m_data, 0, m_data, 0, m_data.length);
        } else {
            m_data = null;
        }
    }

    public void close(float timeoutInSeconds) throws NotImplemented, IncorrectState {
        m_data = null;
    }

    public int getSize() throws NotImplemented, IncorrectState {
        if (m_data != null) {
            return m_data.length;
        } else {
            return -1;
        }
    }
    public abstract void setSize(int size) throws NotImplemented, BadParameter, IncorrectState;

    /** SAGA implementation must use this method instead of setSize() */
    public abstract void setSizeFromSAGAImplementation(int size);

    public abstract byte[] getData() throws NotImplemented, DoesNotExist, IncorrectState;
    public abstract void setData(byte[] data) throws NotImplemented, BadParameter, IncorrectState;

    /** SAGA implementation must use this method instead of getData() */
    public abstract byte[] getDataFromSAGAImplementation();
}
