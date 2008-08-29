package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.introspector.Introspector;
import fr.in2p3.jsaga.introspector.IntrospectorFactory;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IntrospectorFactoryImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IntrospectorFactoryImpl extends IntrospectorFactory {
    protected Introspector doCreateNSIntrospector() throws NotImplemented, NoSuccess {
        return new NSIntrospectorImpl();
    }

    protected Introspector doCreateJobIntrospector() throws NotImplemented, NoSuccess {
        return new JobIntrospectorImpl();
    }
}
