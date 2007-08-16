package fr.in2p3.jsaga.engine.data;

import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstanceRef;
import fr.in2p3.jsaga.engine.security.ContextImpl;
import fr.in2p3.jsaga.engine.security.ContextSelector;
import org.ogf.saga.URI;
import org.ogf.saga.error.*;
import org.ogf.saga.session.Session;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataContextSelector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   25 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataContextSelector extends ContextSelector {
    public DataContextSelector(Session session) {
        super(session);
    }

    public String selectContextByURI_as_fragment(URI uri) throws BadParameter, NoSuccess {
        ContextInstanceRef ref = this.selectContextInstanceRef(uri);
        if (ref.getName() != null) {
            return ref.getName();
        } else {
            return ref.getType()+"["+ref.getIndice()+"]";
        }
    }

    public ContextImpl selectContextByURI(URI uri) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        ContextInstanceRef ref = this.selectContextInstanceRef(uri);
        if (ref != null) {
            return super.selectContextByTypeIndice(ref.getType(), ""+ref.getIndice());
        } else {
            return null;
        }
    }

    private ContextInstanceRef selectContextInstanceRef(URI uri) throws BadParameter, NoSuccess {
        ProtocolEngineConfiguration config = Configuration.getInstance().getConfigurations().getProtocolCfg();
        ContextInstanceRef[] candidates = config.listContextInstanceCandidates(uri);
        switch(candidates.length) {
            case 0:
                return null;
            case 1:
                return candidates[0];
            default:
                //todo: try all candidates to resolve ambiguity
                throw new AmbiguityException("Several contexts matched for URI:"+uri.toString());
        }
    }
}
