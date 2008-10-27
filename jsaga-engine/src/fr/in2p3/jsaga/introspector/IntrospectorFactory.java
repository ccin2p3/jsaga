package fr.in2p3.jsaga.introspector;

import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.introspector.IntrospectorFactoryImpl;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IntrospectorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class IntrospectorFactory {

    private static IntrospectorFactory factory;

    private static synchronized void initializeFactory()
        throws NotImplementedException, ConfigurationException {
        if (factory == null) {
            factory = new IntrospectorFactoryImpl();
        }
    }

    /**
     * Creates an introspector for namespaces. To be provided by the implementation.
     * @return the created introspector.
     */
    protected abstract Introspector doCreateNSIntrospector()
        throws NotImplementedException, NoSuccessException;

    /**
     * Creates an introspector for jobs. To be provided by the implementation.
     * @return the created introspector.
     */
    protected abstract Introspector doCreateJobIntrospector()
        throws NotImplementedException, NoSuccessException;

    /**
     * Creates an introspector for namespaces.
     * @return the created introspector.
     */
    public static Introspector createNSIntrospector() throws NotImplementedException, NoSuccessException {
        initializeFactory();
        return factory.doCreateNSIntrospector();
    }

    /**
     * Creates an introspector for jobs.
     * @return the created introspector.
     */
    public static Introspector createJobIntrospector() throws NotImplementedException, NoSuccessException {
        initializeFactory();
        return factory.doCreateJobIntrospector();
    }
}
