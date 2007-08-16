package fr.in2p3.jsaga.engine.base;

import org.ogf.saga.SagaBase;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BufferImplApplicationManaged
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   7 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BufferImplApplicationManaged extends BufferImpl {
    /** constructor */
    public BufferImplApplicationManaged(byte[] data) {
        super(data);
    }

    /** constructor for deepCopy */
    public BufferImplApplicationManaged(BufferImplApplicationManaged source) {
        super(source);
    }
    public SagaBase deepCopy() {
        return new BufferImplApplicationManaged(this);
    }

    public void setSize(int size) throws NotImplemented, BadParameter, IncorrectState {
        throw new IncorrectState("Not allowed to change the size of a buffer managed by the application", this);
    }

    public void setSizeFromSAGAImplementation(int size) {
        // ignore (because not supported)
    }

    public void setData(byte[] data) throws NotImplemented, BadParameter, IncorrectState {
        m_data = data;        
    }

    public byte[] getData() throws NotImplemented, DoesNotExist, IncorrectState {
        return m_data;        
    }

    public byte[] getDataFromSAGAImplementation() {
        return m_data;
    }
}
