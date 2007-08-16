package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.engine.security.ContextImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.Exception;
import org.ogf.saga.error.NoSuccess;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextFactoryImpl extends ContextFactory {
    protected Context doCreateContext() throws NoSuccess {
        try {
            return new ContextImpl();
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
