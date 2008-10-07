package fr.in2p3.jsaga.engine.factories;

import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.config.ConfigurationException;
import fr.in2p3.jsaga.engine.config.bean.ContextEngineConfiguration;
import fr.in2p3.jsaga.helpers.StringArray;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;
import org.ogf.saga.session.Session;

import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ServiceAdaptorFactory
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ServiceAdaptorFactory {
    private ContextEngineConfiguration m_config;

    protected ServiceAdaptorFactory(ContextEngineConfiguration config) {
        m_config = config;
    }

    protected ContextImpl findContext(Session session, String contextRef) throws NotImplemented, NoSuccess {
        if (session != null) {
            Context[] contextArray = session.listContexts();
            for (int i=0; contextArray!=null && i<contextArray.length; i++) {
                try {
                    if (contextRef.equals(contextArray[i].getAttribute(Context.TYPE))) {
                        return (ContextImpl) contextArray[i];
                    }
                } catch(Exception e) {
                    throw new NoSuccess(e.getMessage(), contextArray[i]);
                }
            }
        }
        return null;
    }

    protected ContextImpl findContext(Session session, String[] supportedContextTypeArray) throws NotImplemented, NoSuccess {
        Set<String> contextRefCandidates = new HashSet<String>();
        for (int i=0; supportedContextTypeArray!=null && i<supportedContextTypeArray.length; i++) {
            if ("None".equals(supportedContextTypeArray[i])) {
                return null;
            }
            fr.in2p3.jsaga.engine.schema.config.Context[] configArray = m_config.listContextsArrayByType(supportedContextTypeArray[i]);
            for (int c=0; configArray!=null && c<configArray.length; c++) {
                contextRefCandidates.add(configArray[c].getName());
            }
        }

        List<ContextImpl> contextCandidates = new ArrayList<ContextImpl>();
        for (Iterator<String> it=contextRefCandidates.iterator(); it.hasNext(); ) {
            String contextRef = it.next();
            ContextImpl context = this.findContext(session, contextRef);
            if (context == null) {
                throw new ConfigurationException("INTERNAL ERROR: effective-config may be inconsistent");
            }
            try {
                context.getAdaptor();
                contextCandidates.add(context);
            } catch(org.ogf.saga.error.Exception e) {
                // ignore invalid contexts
            }
        }

        switch(contextCandidates.size()) {
            case 0:
                return null;
            case 1:
                return contextCandidates.get(0);
            default:
                String[] candidateRefs = new String[contextCandidates.size()];
                for (int i=0; i<candidateRefs.length; i++) {
                    try {
                        candidateRefs[i] = contextCandidates.get(i).getAttribute(Context.TYPE);
                    } catch (Exception e) {
                        throw new AmbiguityException("Found several valid security contexts");
                    }
                }
                throw new AmbiguityException("Found several valid security contexts: "+StringArray.arrayToString(candidateRefs,", "));
        }
    }

    protected String getContextType(Context context) {
        try {
            return context.getAttribute(Context.TYPE);
        } catch (Exception e) {
            return "error ["+e.getMessage()+"]";
        }
    }
}
