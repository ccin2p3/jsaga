package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BufferImplSagaManaged
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BufferImplSagaManaged extends BufferImpl {
    private static final int DEFAULT_SIZE_TO_CREATE = 1024;
    private int m_sizeToCreate;     // for SAGA implementation managed buffers

    /** constructor */
    public BufferImplSagaManaged() {
        super((byte[]) null);
        m_sizeToCreate = DEFAULT_SIZE_TO_CREATE;
    }

    /** constructor for deepCopy */
    public BufferImplSagaManaged(BufferImplSagaManaged source) {
        super(source);
        m_sizeToCreate = source.m_sizeToCreate;
    }
    public SagaBase deepCopy() {
        return new BufferImplSagaManaged(this);
    }

    public void setSize(int size) throws NotImplemented, BadParameter, IncorrectState {
        this.setSizeFromSAGAImplementation(size);
    }

    public void setSizeFromSAGAImplementation(int size) {
        if (m_data != null) {
            if (size == m_data.length) {
                // do nothing
            } else {
                // create new buffer and copy data
                byte[] saved = m_data;
                m_data = new byte[size];
                int minSize = (size<saved.length ? size : saved.length);
                System.arraycopy(saved, 0, m_data, 0, minSize);
            }
        } else {
            // set size of bytes array to create
            m_sizeToCreate = size;
        }
    }

    public void setData(byte[] data) throws NotImplemented, BadParameter, IncorrectState {
        throw new IncorrectState("Not allowed to change a buffer managed by SAGA implementation", this);
    }

    public byte[] getData() throws NotImplemented, DoesNotExist, IncorrectState {
        if (m_data == null) {
            throw new IncorrectState("No I/O operation has been successfully performed on the buffer yet", this);
        }
        return m_data;
    }

    public byte[] getDataFromSAGAImplementation() {
        if (m_data == null) {
            m_data = new byte[m_sizeToCreate];
        }
        return m_data;
    }
}
