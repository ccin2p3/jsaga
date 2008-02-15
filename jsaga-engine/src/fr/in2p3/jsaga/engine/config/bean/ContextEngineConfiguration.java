package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.UserAttributesMap;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.Attribute;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import org.ogf.saga.error.DoesNotExist;
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
    private ContextInstance[] m_contextInstance;

    public ContextEngineConfiguration(ContextInstance[] config, SecurityAdaptorDescriptor desc, UserAttributesMap userAttributes) throws Exception {
        m_contextInstance = config;
        for (int i=0; m_contextInstance!=null && i<m_contextInstance.length; i++) {
            ContextInstance ctx = m_contextInstance[i];

            // update configured attributes with user attributes
            this.updateAttributes(userAttributes, ctx, ctx.getType());  //common to all instances of this type
            this.updateAttributes(userAttributes, ctx, ctx.getName());

            // correct configured attributes according to usage
            Usage usage = desc.getUsage(ctx.getType());
            if (usage != null) {
                for (int a=0; a<ctx.getAttributeCount(); a++) {
                    Attribute attr = ctx.getAttribute(a);
                    if (attr.getValue() != null) {
                        try {
                            attr.setValue(usage.correctValue(attr.getName(), attr.getValue()));
                        } catch(DoesNotExist e) {}
                    }
                }
            }
        }
    }
    private void updateAttributes(UserAttributesMap userAttributes, ContextInstance ctx, String id) {
        Attribute[] attributes = userAttributes.update(ctx.getAttribute(), id);
        if (attributes != null) {
            ctx.setAttribute(attributes);
        }
    }

    /**
     * @param id may be a context or a context type
     * @throws NoSuccess if no match or ambiguity
     */
    public ContextInstance findContextInstance(String id) throws NoSuccess {
        if (id != null) {
            ContextInstance[] ctxArray = this.listContextInstanceArray(id);
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
    public ContextInstance[] listContextInstanceArray(String id) throws NoSuccess {
        if (id != null) {
            // try by name
            ContextInstance ctx = this.findContextInstanceByName(id);
            if (ctx != null) {
                return new ContextInstance[]{ctx};
            } else {
                // try by type
                return this.listContextInstanceArrayByType(id);
            }
        } else {
            throw new NoSuccess("Null context identifier");
        }
    }

    /**
     * @return null if no match
     * @throws NoSuccess if null context name
     */
    public ContextInstance findContextInstanceByName(String name) throws NoSuccess {
        if (name != null) {
            for (int c=0; c<m_contextInstance.length; c++) {
                ContextInstance xmlInstance = m_contextInstance[c];
                if (xmlInstance.getName()!=null && xmlInstance.getName().equals(name)) {
                    return xmlInstance;
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
    ContextInstance[] listContextInstanceArrayByType(String type) throws NoSuccess {
        if (type != null) {
            List list = new ArrayList();
            for (int c=0; c<m_contextInstance.length; c++) {
                ContextInstance xmlInstance = m_contextInstance[c];
                if (xmlInstance.getType().equals(type)) {
                    list.add(xmlInstance);
                }
            }
            return (ContextInstance[]) list.toArray(new ContextInstance[list.size()]);
        } else {
            throw new NoSuccess("Null context type");
        }
    }

    public ContextInstance[] toXMLArray() {
        return m_contextInstance;
    }
}
