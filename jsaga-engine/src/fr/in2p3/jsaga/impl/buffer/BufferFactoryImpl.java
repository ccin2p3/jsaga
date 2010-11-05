package fr.in2p3.jsaga.impl.buffer;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BufferFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BufferFactoryImpl extends BufferFactory {
    protected Buffer doCreateBuffer(byte[] data) throws BadParameterException, NoSuccessException {
        return new ApplicationAllocatedBufferImpl(data);
    }

    protected Buffer doCreateBuffer(int size) throws BadParameterException, NoSuccessException {
        return new ImplementationAllocatedBufferImpl(size);
    }
}
