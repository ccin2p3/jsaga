package integration;

import fr.in2p3.jsaga.EngineProperties;
import org.ogf.saga.context.ContextInitTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobCollectionContextInit
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   8 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobCollectionContextInit extends ContextInitTest {
    public JobCollectionContextInit() throws Exception {
        super(configureEngine("EGEE"));
    }

    private static String configureEngine(String contextId) {
        // configure JSAGA engine
        System.setProperty("debug", "true");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE, "../test/resources/jobcollection/jsaga-universe.xml");
        EngineProperties.setProperty(EngineProperties.JSAGA_UNIVERSE_ENABLE_CACHE, "false");
        EngineProperties.setProperty(EngineProperties.JOB_CONTROL_CHECK_AVAILABILITY, "false");
        // returns
        return contextId;
    }
}
