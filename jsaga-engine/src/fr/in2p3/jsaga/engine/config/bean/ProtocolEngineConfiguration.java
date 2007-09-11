package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.*;
import fr.in2p3.jsaga.engine.config.adaptor.DataAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.*;
import fr.in2p3.jsaga.helpers.StringArray;
import org.ogf.saga.URI;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;

import java.util.ArrayList;
import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ProtocolEngineConfiguration
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   23 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ProtocolEngineConfiguration {
    private Protocol[] m_protocol;

    public ProtocolEngineConfiguration(Protocol[] protocol, DataAdaptorDescriptor desc, UserAttributesMap userAttributes) throws Exception {
        m_protocol = protocol;
        for (int i=0; m_protocol!=null && i<m_protocol.length; i++) {
            Protocol prt = m_protocol[i];

            // get attributes
            ConfAttributesMap attrs = new ConfAttributesMap(prt.getAttribute());

            // update configured attributes with usages
            Usage usage = desc.getUsage(prt.getScheme());
            if (usage != null) {
                usage.updateAttributes(attrs.getMap());
            }

            // set new attributes
            prt.setAttribute(attrs.toArray());

            // update configured attributes with user attributes
            this.updateAttributes(userAttributes, prt, prt.getScheme());
            for (int a=0; a<prt.getSchemeAliasCount(); a++) {
                this.updateAttributes(userAttributes, prt, prt.getSchemeAlias(a));
            }
        }
    }
    private void updateAttributes(UserAttributesMap userAttributes, Protocol protocol, String id) {
        Attribute[] attributes = userAttributes.update(protocol.getAttribute(), id);
        if (attributes != null) {
            protocol.setAttribute(attributes);
        }
    }

    public Protocol[] toXMLArray() {
        return m_protocol;
    }

    public Protocol findProtocol(String scheme) throws NoSuccess {
        for (int p=0; p<m_protocol.length; p++) {
            Protocol protocol = m_protocol[p];
            if (protocol.getScheme().equals(scheme) || StringArray.arrayContains(protocol.getSchemeAlias(), scheme)) {
                return protocol;
            }
        }
        throw new NoSuccess("No protocol matches scheme: "+scheme);
    }

    /**
     * Find the context to be used with <code>uri</code>
     */
    public ContextInstanceRef[] listContextInstanceCandidates(URI uri) throws BadParameter, NoSuccess {
        if (uri != null) {
            return this.listContextInstanceCandidates(
                    findProtocol(uri.getScheme()),
                    uri.getHost(),
                    uri.getFragment());
        } else {
            throw new BadParameter("URI is null");
        }
    }

    public ContextInstanceRef[] listContextInstanceCandidates(Protocol protocol, String hostname, String fragment) throws NoSuccess {
        ContextEngineConfiguration config = Configuration.getInstance().getConfigurations().getContextCfg();
        if (fragment != null) {
            ContextInstance[] ctxArray = config.listContextInstanceArrayById(fragment);
            switch(ctxArray.length) {
                case 0:
                    throw new NoSuccess("No context instance matches: "+fragment);
                case 1:
                    return new ContextInstanceRef[]{toContextInstanceRef(ctxArray[0])};
                default:
                    // try to restrict with context instances filtered by hostname
                    ContextInstanceRef[] ctxArrayByHostname = this.listContextInstancesByHostname(protocol, hostname);
                    if (ctxArrayByHostname.length > 0) {
                        return ctxArrayByHostname;
                    } else {
                        return toContextInstanceRefArray(ctxArray);
                    }
            }
        } else {
            // restrict with context instances filtered by hostname
            ContextInstanceRef[] ctxArrayByHostname = this.listContextInstancesByHostname(protocol, hostname);
            if (ctxArrayByHostname.length > 0) {
                return ctxArrayByHostname;
            } else {
                // if no context instance is configured, then all supported context instances are eligible
                List list = new ArrayList();
                for (int c=0; c<protocol.getSupportedContextTypeCount(); c++) {
                    String type = protocol.getSupportedContextType(c);
                    ContextInstance[] ctxArray = config.listContextInstanceArray(type);
                    ContextInstanceRef[] refArray = toContextInstanceRefArray(ctxArray);
                    for (int i=0; i<refArray.length; i++) {
                        list.add(refArray[i]);
                    }
                }
                return (ContextInstanceRef[]) list.toArray(new ContextInstanceRef[list.size()]);
            }
        }
    }

    private ContextInstanceRef[] listContextInstancesByHostname(Protocol protocol, String hostname) throws NoSuccess {
        for (int d=0; d<protocol.getDomainCount(); d++) {
            Domain domain = protocol.getDomain(d);
            if (hostname.endsWith("."+domain.getName())) {
                for (int h=0; h<domain.getHostCount(); h++) {
                    Host host = domain.getHost(h);
                    if (hostname.startsWith(host.getName())) {
                        return host.getContextInstanceRef();
                    }
                }
                return domain.getContextInstanceRef();
            }
        }
        return protocol.getContextInstanceRef();
    }

    private static ContextInstanceRef[] toContextInstanceRefArray(ContextInstance[] ctxArray) {
        ContextInstanceRef[] refArray = new ContextInstanceRef[ctxArray.length];
        for (int i=0; i<ctxArray.length; i++) {
            refArray[i] = toContextInstanceRef(ctxArray[i]);
        }
        return refArray;
    }
    private static ContextInstanceRef toContextInstanceRef(ContextInstance ctx) {
        ContextInstanceRef ref = new ContextInstanceRef();
        ref.setType(ctx.getType());
        ref.setIndice(ctx.getIndice());
        ref.setName(ctx.getName());
        return ref;
    }
}
