package fr.in2p3.jsaga.impl.context;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
import fr.in2p3.jsaga.generated.session.Attribute;

public class ConfigurableContextFactory {
	
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
		        		Context context = ContextFactory.createContext(sessionContextsCfg[i].getType());
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
