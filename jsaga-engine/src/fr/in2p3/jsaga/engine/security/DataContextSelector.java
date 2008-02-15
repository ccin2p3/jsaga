package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.config.bean.ProtocolEngineConfiguration;
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

    public ContextImpl selectContextByURI(URL url) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, BadParameter, Timeout, NoSuccess {
        ProtocolEngineConfiguration config = Configuration.getInstance().getConfigurations().getProtocolCfg();
        ContextInstanceRef[] refArray = config.listContextInstanceCandidates(url);
        switch(refArray.length) {
            case 0:
            {
                return null;
            }
            case 1:
            {
                ContextInstanceRef ref = refArray[0];
                ContextImpl context = super.selectContextByName(ref.getName());
                try {
                    context.getAdaptor();
                } catch(Exception e) {
                    throw new NoSuccess("Invalid context: "+ref.getName(), e);
                }
                return context;
            }
            default:
            {
                List ctxList = new ArrayList();
                for (int i=0; i<refArray.length; i++) {
                    ContextInstanceRef ref = refArray[i];
                    ContextImpl context = super.selectContextByName(ref.getName());
                    try {
                        context.getAdaptor();
                        ctxList.add(context);
                    } catch(Exception e) {
                        // ignore invalid contexts
                    }
                }
                switch(ctxList.size()) {
                    case 0:
                        throw new NoSuccess("None of the candidate security contexts is valid for URL: "+url.toString());
                    case 1:
                        return (ContextImpl) ctxList.get(0);
                    default:
                        throw new AmbiguityException("Several contexts matched for URL:"+url.toString());
                }
            }
        }
    }
}
