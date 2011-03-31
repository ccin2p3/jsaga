package fr.in2p3.jsaga.impl.context;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;
import fr.in2p3.jsaga.EngineProperties;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;

public class ConfigurableContextFactory {
	
    public static String[] listContextUrlPrefix() throws ConfigurationException {
    	SessionConfiguration cfg = new SessionConfiguration(EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS));
    	fr.in2p3.jsaga.generated.session.Context[] sessionContextsCfg = cfg.getSessionContextsCfg();
    	String[] urlPrefixes = new String[sessionContextsCfg.length];
    	for (int i=0; i<sessionContextsCfg.length; i++) {
    		urlPrefixes[i] = sessionContextsCfg[i].getAttribute()[0].getValue();
    	}
    	return urlPrefixes;
    }
    
    public static Context createContext(String id) throws IncorrectStateException, TimeoutException, NoSuccessException {
    	SessionConfiguration cfg = new SessionConfiguration(EngineProperties.getURL(EngineProperties.JSAGA_DEFAULT_CONTEXTS));
    	fr.in2p3.jsaga.generated.session.Context[] sessionContextsCfg = cfg.getSessionContextsCfg();
    	for (int i=0; i<sessionContextsCfg.length; i++) {
        	//if (id.equals(sessionContextsCfg[i].getId()) || id.equals(sessionContextsCfg[i].getType() + String.valueOf(i+1))) {
          	if (id.equals(sessionContextsCfg[i].getAttribute()[0].getValue())) {
        		// create SAGA context
        		Context context = ContextFactory.createContext(sessionContextsCfg[i].getType());
        		// set attributes
        		SessionConfiguration.setDefaultContext(context, sessionContextsCfg[i]);
        		return context;
        	}
        }
    	throw new NoSuccessException("No context with id: " + id);
    }

}
