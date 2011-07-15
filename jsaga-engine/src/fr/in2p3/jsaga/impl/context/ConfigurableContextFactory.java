package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.generated.session.Attribute;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ConfigurableContextFactory
* Author: Lionel Schwarz (lionel.schwarz@in2p3.fr)
* Date:   31/03/2011
* ***************************************************
* Description:                                      */

public class ConfigurableContextFactory {
    private static final String JSAGA_FACTORY = Base.getSagaFactory();

    public static Context[] getContextsOfDefaultSession() throws IncorrectStateException, TimeoutException, NoSuccessException {
        SessionConfiguration cfg = new SessionConfiguration(EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS));
        fr.in2p3.jsaga.generated.session.Context[] sessionContextsCfg = cfg.getSessionContextsCfg();
        Context[] sessionContexts = new Context[sessionContextsCfg.length];
        for (int i=0; i<sessionContextsCfg.length; i++) {
            // create SAGA context
            sessionContexts[i] = ContextFactory.createContext(JSAGA_FACTORY, sessionContextsCfg[i].getType());
            // set attributes
            SessionConfiguration.setDefaultContext(sessionContexts[i], sessionContextsCfg[i]);
        }
        return sessionContexts;
    }
	
    public static ConfiguredContext[] listConfiguredContext() throws ConfigurationException {
    	SessionConfiguration cfg = new SessionConfiguration(EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS));
    	fr.in2p3.jsaga.generated.session.Context[] sessionContextsCfg = cfg.getSessionContextsCfg();
    	ConfiguredContext[] cfgContexts = new ConfiguredContext[sessionContextsCfg.length];
    	for (int i=0; i<sessionContextsCfg.length; i++) {
    		for (Attribute attr : sessionContextsCfg[i].getAttribute()) {
    			if (ContextImpl.URL_PREFIX.equals(attr.getName())) {
    	    		cfgContexts[i] = new ConfiguredContext(attr.getValue(), sessionContextsCfg[i].getType());
    	    		break;
    			}
    		}
    	}
    	return cfgContexts;
    }
    
    public static Context createContext(ConfiguredContext ctx) throws IncorrectStateException, TimeoutException, NoSuccessException {
    	SessionConfiguration cfg = new SessionConfiguration(EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS));
    	fr.in2p3.jsaga.generated.session.Context[] sessionContextsCfg = cfg.getSessionContextsCfg();
    	for (int i=0; i<sessionContextsCfg.length; i++) {
    		for (Attribute attr : sessionContextsCfg[i].getAttribute()) {
    			if (ContextImpl.URL_PREFIX.equals(attr.getName())) {
		          	if (ctx.getUrlPrefix().equals(attr.getValue())) {
		        		// create SAGA context
		        		Context context = ContextFactory.createContext(JSAGA_FACTORY, sessionContextsCfg[i].getType());
		        		// set attributes
		        		SessionConfiguration.setDefaultContext(context, sessionContextsCfg[i]);
		        		return context;
		        	}
    			}
    		}
        }
    	throw new NoSuccessException("No context with id: " + ctx.getUrlPrefix());
    }

}
