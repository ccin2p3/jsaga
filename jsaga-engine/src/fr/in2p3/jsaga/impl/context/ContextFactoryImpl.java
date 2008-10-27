package fr.in2p3.jsaga.impl.context;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextFactoryImpl extends ContextFactory {
    protected Context doCreateContext(String type) throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        return new ContextImpl(type);
    }
}
