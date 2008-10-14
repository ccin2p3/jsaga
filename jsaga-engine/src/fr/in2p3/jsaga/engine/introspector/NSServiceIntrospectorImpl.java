package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.ContextEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.Context;
import fr.in2p3.jsaga.engine.schema.config.DataService;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.error.*;

import java.util.HashSet;
import java.util.Set;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   NSServiceIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   29 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class NSServiceIntrospectorImpl extends AbstractIntrospectorImpl implements Introspector {
    private DataService m_config;

    public NSServiceIntrospectorImpl(DataService config) throws NoSuccess {
        super(config.getName());
        m_config = config;
    }

    protected String getChildIntrospectorType() {
        return Introspector.CONTEXT;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        for (int i=0; i<m_config.getAttributeCount(); i++) {
            if (m_config.getAttribute(i).getName().equals(key)) {
                return m_config.getAttribute(i).getValue();
            }
        }
        throw new DoesNotExist("Attribute "+key+" does not exist", this);
    }

    /** @return contexts */
    protected String[] getChildIntrospectorKeys() throws NoSuccess {
        Set<String> result = new HashSet<String>();
        if (m_config.getContextRef() != null) {
            result.add(m_config.getContextRef());
        } else {
            ContextEngineConfiguration ctxConfig = Configuration.getInstance().getConfigurations().getContextCfg();
            for (String type : m_config.getSupportedContextType()) {
                Context[] contexts = ctxConfig.listContextsArrayByType(type);
                for (Context ctx : contexts) {
                    result.add(ctx.getName());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /** Throw NotImplemented exception */
    public Introspector getChildIntrospector(String key) throws NotImplemented, DoesNotExist, NoSuccess {
        throw new NotImplemented("Please use SAGA interface for introspecting security contexts");
    }
}
