package fr.in2p3.jsaga.engine.config.bean;

import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.engine.config.AmbiguityException;
import fr.in2p3.jsaga.engine.config.UserAttributesMap;
import fr.in2p3.jsaga.engine.config.adaptor.SecurityAdaptorDescriptor;
import fr.in2p3.jsaga.engine.schema.config.*;
import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.error.NoSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            this.updateAttributes(userAttributes, ctx, ctx.getType()+"["+ctx.getIndice()+"]");
            this.updateAttributes(userAttributes, ctx, ctx.getName());

            // get usage and init usage
//todo: split attributes in two parts (usage and initUsage)
/* WORKAROUND to bug (initUsage.UserProxy may be removed if usage.<UserProxy> and proxy file does not exist yet)
            Usage usage = desc.getUsage(ctx.getType());
            if (usage != null) {
                usage.resetWeight();
            }
*/
            Usage initUsage = desc.getInitUsage(ctx.getType());
            if (initUsage != null) {
                initUsage.resetWeight();
            }

            // correct configured attributes according to usage and init usage
            for (int a=0; a<ctx.getAttributeCount(); a++) {
                Attribute attr = ctx.getAttribute(a);
/*
                if (usage != null) {
                    try {
                        attr.setValue(usage.correctValue(attr.getName(), attr.getValue(), attr.getSource().getType()));
                    } catch(DoesNotExist e) {}
                }
*/
                if (initUsage != null) {
                    try {
                        attr.setValue(initUsage.correctValue(attr.getName(), attr.getValue(), attr.getSource().getType()));
                    } catch(DoesNotExist e) {/*do nothing*/}
                }
            }

            // remove ambiguity
            for (int a=0; a<ctx.getAttributeCount(); a++) {
                Attribute attr = ctx.getAttribute(a);
/*
                if (usage!=null && usage.removeValue(attr.getName())) {
                    attr.setValue(null);
                }
*/
                if (initUsage!=null && initUsage.removeValue(attr.getName())) {
                    attr.setValue(null);
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
     * @throws NoSuccess if no match
     */
    public ContextInstance findContextInstance(ContextInstanceRef ref) throws NoSuccess {
        return findContextInstance(ref.getType(), ref.getIndice());
    }

    /**
     * @throws NoSuccess if no match
     */
    public ContextInstance findContextInstance(String type, int indice) throws NoSuccess {
        return findContextInstance(type, new Integer(indice));
    }

    /**
     * @throws NoSuccess if no match
     */
    public ContextInstance findContextInstance(String type, String indice) throws NoSuccess {
        return findContextInstance(type, indice!=null ? Integer.valueOf(indice) : null);
    }

    /**
     * @throws NoSuccess if no match
     */
    public ContextInstance findContextInstance(String type, Integer indice) throws NoSuccess {
        ContextInstance[] matching = listContextInstanceArray(type);
        if (indice != null) {
            if (matching.length == 0) {
                throw new NoSuccess("No context instance matches type: "+type);
            } else if (matching.length <= indice.intValue()) {
                throw new NoSuccess("Indice "+indice+" is out of bound for context type: "+type);
            } else {
                return matching[indice.intValue()];
            }
        } else if (matching.length == 1) {
            return matching[0];
        } else {
            throw new AmbiguityException("Several context instance match: "+type);
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
    public ContextInstance[] listContextInstanceArray(String type) throws NoSuccess {
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

    /**
     * @throws NoSuccess if no match
     */
    public ContextInstance[] listContextInstanceArrayById(String id) throws NoSuccess {
        if (id != null) {
            // list context instances matching fragment
            Matcher m = Pattern.compile("(.+)\\[(\\d+)\\]").matcher(id);
            if (m.matches()) {
                // try by type[indice]
                String type = m.group(1);
                int indice = Integer.parseInt(m.group(2));
                return new ContextInstance[]{this.findContextInstance(type, indice)}; //found
            } else {
                // try by name
                ContextInstance ctx = this.findContextInstanceByName(id);
                if (ctx != null) {
                    return new ContextInstance[]{ctx}; //found
                } else {
                    // try by type
                    ContextInstance[] ctxArray = this.listContextInstanceArray(id);
                    if (ctxArray.length > 0) {
                        return ctxArray; //found
                    } else {
                        throw new NoSuccess("No context instance matches: "+id);
                    }
                }
            }
        } else {
            throw new NoSuccess("Null context identifier");
        }
    }

    public ContextInstance[] toXMLArray() {
        return m_contextInstance;
    }
}
