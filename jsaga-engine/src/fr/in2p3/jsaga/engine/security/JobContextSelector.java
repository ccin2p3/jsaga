package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.JobserviceEngineConfiguration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstanceRef;
import fr.in2p3.jsaga.impl.context.ContextImpl;
import org.ogf.saga.URL;
import org.ogf.saga.error.*;
import org.ogf.saga.error.Exception;
import org.ogf.saga.session.Session;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JobContextSelector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   19 nov. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JobContextSelector extends ContextSelector {
    public JobContextSelector(Session session) {
        super(session);
    }

    public String selectContextByURI_as_fragment(URL url) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        ContextImpl context = this.selectContextByURI(url);
        return context.getContextId();
    }

    public ContextImpl selectContextByURI(URL url) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        ContextImpl[] candidates = this.listContextByURI(url);
        switch(candidates.length) {
            case 0:
                return null;
            case 1:
                return candidates[0];
            default:
                //todo: try all candidates to resolve ambiguity
                throw new AmbiguityException("Several contexts matched for URL:"+url.toString());
        }
    }

    private ContextImpl[] listContextByURI(URL url) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, IncorrectState, Timeout, NoSuccess {
        JobserviceEngineConfiguration config = Configuration.getInstance().getConfigurations().getJobserviceCfg();
        ContextInstanceRef[] refArray = config.listContextInstanceCandidates(url);
        if (refArray.length > 0) {
            List ctxList = new ArrayList();
            for (int i=0; i<refArray.length; i++) {
                ContextInstanceRef ref = refArray[i];
                ContextImpl context = super.selectContextByTypeIndice(ref.getType(), ""+ref.getIndice());
                try {
                    context.createSecurityAdaptor();
                    ctxList.add(context);
                } catch(Exception e) {/*ignore*/}
            }
            switch(ctxList.size()) {
                case 0:
                    throw new NoSuccess("None of the candidate security contexts is valid for URL: "+url.toString());
                default:
                    return (ContextImpl[]) ctxList.toArray(new ContextImpl[ctxList.size()]);
            }
        } else {
            return new ContextImpl[0];
        }
    }
}
