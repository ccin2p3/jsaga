package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.engine.schema.config.Context;
import org.ogf.saga.error.NoSuccess;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextEngineConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextEngineConfiguration {
    private Context[] m_context;

    public ContextEngineConfiguration(Context[] config) {
        m_context = config;
    }

    /**
     * @param id may be a context or a context type
     * @throws NoSuccess if no match or ambiguity
     */
    public Context findContext(String id) throws NoSuccess {
        if (id != null) {
            Context[] ctxArray = this.listContextsArray(id);
            switch(ctxArray.length) {
                case 0:
                    throw new NoSuccess("No context instance matches: "+id);
                case 1:
                    return ctxArray[0];
                default:
                    throw new NoSuccess("Ambiguity, please use context name instead of type: "+id);
            }
        } else {
            throw new NoSuccess("Null context identifier");
        }
    }

    /**
     * @param id may be a context or a context type
     */
    public Context[] listContextsArray(String id) throws NoSuccess {
        if (id != null) {
            // try by name
            Context ctx = this.findContextByName(id);
            if (ctx != null) {
                return new Context[]{ctx};
            } else {
                // try by type
                return this.listContextsArrayByType(id);
            }
        } else {
            throw new NoSuccess("Null context identifier");
        }
    }

    /**
     * @return null if no match
     * @throws NoSuccess if null context name
     */
    public Context findContextByName(String name) throws NoSuccess {
        if (name != null) {
            for (int c=0; c<m_context.length; c++) {
                Context context = m_context[c];
                if (context.getName()!=null && context.getName().equals(name)) {
                    return context;
                }
            }
            return null;
        } else {
            throw new NoSuccess("Null context name");
        }
    }

    /**
     * @return empty array if no match
     * @throws NoSuccess if null context type
     */
    public Context[] listContextsArrayByType(String type) throws NoSuccess {
        if (type != null) {
            List list = new ArrayList();
            for (int c=0; c<m_context.length; c++) {
                Context context = m_context[c];
                if (context.getType().equals(type)) {
                    list.add(context);
                }
            }
            return (Context[]) list.toArray(new Context[list.size()]);
        } else {
            throw new NoSuccess("Null context type");
        }
    }

    public Context[] toXMLArray() {
        return m_context;
    }
}
