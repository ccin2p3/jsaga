package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.engine.base.BufferImplApplicationManaged;
import fr.in2p3.jsaga.engine.base.BufferImplSagaManaged;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NotImplemented;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   BufferFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class BufferFactoryImpl extends BufferFactory {
    protected Buffer doCreateBuffer(byte[] data) throws NotImplemented, BadParameter {
        if (data != null) {
            return new BufferImplApplicationManaged(data);
        } else {
            return new BufferImplSagaManaged();
        }
    }
}
