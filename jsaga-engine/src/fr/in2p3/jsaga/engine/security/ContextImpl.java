package fr.in2p3.jsaga.engine.security;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.security.*;
import fr.in2p3.jsaga.adaptor.security.defaults.Default;
import fr.in2p3.jsaga.adaptor.security.usage.Usage;
import fr.in2p3.jsaga.engine.adaptor.SecurityAdaptorBuilderFactory;
import fr.in2p3.jsaga.engine.base.AbstractAttributesImpl;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogf.saga.SagaBase;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.lang.Exception;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class ContextImpl extends AbstractAttributesImpl implements Context {
    private static Log s_logger = LogFactory.getLog(ContextImpl.class);
    private SecurityAdaptorBuilder m_adaptorBuilder;
    private SecurityAdaptor m_adaptor;

    /** constructor */
    public ContextImpl() throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        super(null, true);  //not attached to a session, isExtensible=true
        super.setAttribute("Type", "Unknown");
        m_adaptorBuilder = null;
        m_adaptor = null;
    }

    /** constructor for deepCopy */
    protected ContextImpl(ContextImpl source) {
        super(source);
        m_adaptorBuilder = source.m_adaptorBuilder;
        m_adaptor = source.m_adaptor;
    }
    public SagaBase deepCopy() {
        return new ContextImpl(this);
    }

    /** overload super.setAttribute() */
    public String setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        if (!this.isInitialized(key)) {
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(value);
        } else if (key.equals("Type")) {
            throw new IncorrectState("Not allowed to change the type of context: "+ m_adaptorBuilder.getType(), this);
        }
        return super.setAttribute(key, value);
    }
    /** overload super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        this.isInitialized(key);
        return super.getAttribute(key);
    }
    /** overload super.setVectorAttribute() */
    public String[] setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        if (!this.isInitialized(key)) {
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(values!=null && values.length>0 ? values[0] : null);
        } else if (key.equals("Type")) {
            throw new IncorrectState("Not allowed to change the type of context: "+ m_adaptorBuilder.getType(), this);
        }
        return super.setVectorAttribute(key, values);
    }
    /** overload super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        this.isInitialized(key);
        return super.getVectorAttribute(key);
    }

    public void setDefaults() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_adaptorBuilder == null) {
            throw new IncorrectState("Please set context type prior to invoking any other operation", this);
        }

        // get context instance configuration
        ContextInstance xmlInstance = Configuration.getInstance().getConfigurations().getContextCfg().findContextInstance(
                super._getOptionalAttribute("Type"),
                super._getOptionalAttribute("Indice"));

        // set context instance identifiers
        if (xmlInstance.hasIndice()) {
            _setAttr("Indice", String.valueOf(xmlInstance.getIndice()));
        }
        if (xmlInstance.getName() != null) {
            _setAttr("Name", xmlInstance.getName());
        }

        // set context instance default attributes
        for (int i=0; xmlInstance!=null && i<xmlInstance.getAttributeCount(); i++) {
            fr.in2p3.jsaga.engine.schema.config.Attribute attr = xmlInstance.getAttribute(i);
            if (! super._containsAttributeKey(attr.getName())) {
                _setAttr(attr.getName(), attr.getValue());
            } else if (attr.getValue() == null) {
                _removeAttr(attr.getName());
            }
        }

        // set context type default attributes
        Default[] defaults = m_adaptorBuilder.getDefaults(super._getAttributesMap());
        for (int i=0; defaults!=null && i<defaults.length; i++) {
            if (! super._containsAttributeKey(defaults[i].getName())) {
                _setAttr(defaults[i].getName(), defaults[i].getValue());
            }
        }
    }

    /**
     * @see org.ogf.saga.session.Session
     */
    public void close() {
        if (m_adaptor != null) {
            try {
                m_adaptor.close();
            } catch (Exception e) {
                s_logger.warn("Failed to close security adaptor", e);
            }
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void init() throws NotImplemented, BadParameter, IncorrectState, NoSuccess {
        if (m_adaptorBuilder == null) {
            throw new IncorrectState("Please set context type prior to invoking any other operation", this);
        }
        if (!(m_adaptorBuilder instanceof InitializableSecurityAdaptorBuilder)) {
            return; //ignore
        }
        InitializableSecurityAdaptorBuilder initAdaptorBuilder = (InitializableSecurityAdaptorBuilder) m_adaptorBuilder;
        this.checkUsage(initAdaptorBuilder.getInitUsage());
        try {
            initAdaptorBuilder.initAndCreateSecurityAdaptor(super._getAttributesMap());
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public SecurityAdaptor createSecurityAdaptor() throws NotImplemented, BadParameter, IncorrectState, NoSuccess {
        this.checkUsage(m_adaptorBuilder.getUsage());
        try {
            m_adaptor = m_adaptorBuilder.createSecurityAdaptor(super._getAttributesMap());
        } catch (NotImplemented e) {
            throw e;
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
        return m_adaptor;
    }

    private boolean isInitialized(String key) throws IncorrectState {
        if (key == null) {
            throw new IncorrectState("Attribute key is null");
        }
        if (m_adaptorBuilder == null) {
            if (key.equals("Type")) {
                return false;
            } else {
                throw new IncorrectState("Please set context type prior to invoking any other operation", this);
            }
        } else {
            if (key.equals("Type") || key.equals("Indice") || key.equals("Name")) {
                return true;
            } else if (m_adaptorBuilder.getUsage().containsName(key)) {
                return true;
            } else if (contains(m_adaptorBuilder.getDefaults(new HashMap()), key)) {
                return true;
            } else {
                throw new IncorrectState("Unexpected attribute name for context of type ["+ m_adaptorBuilder.getType()+"]: "+key, this);
            }
        }
    }
    private boolean contains(Default[] defaults, String key) {
        for (int i=0; i<defaults.length; i++) {
            if (defaults[i].getName().equals(key)) {
                return true;
            }
        }
        return false;
    }

    private void checkUsage(Usage usage) throws NotImplemented, BadParameter, IncorrectState, NoSuccess {
        if (usage != null) {
            Usage missing = usage.getMissingValues(super._getAttributesMap());
            if (missing != null) {
                if (Base.INTERACTIVE) {
                    // prompt for missing values
                    String id = (super._getOptionalAttribute("Name")!=null
                            ? super._getOptionalAttribute("Name")
                            : super._getOptionalAttribute("Type")+"["+ super._getOptionalAttribute("Indice")+"]");
                    try {
                        Map newAttributes = new HashMap();
                        missing.promptForValues(newAttributes, id);
                        for (Iterator it=newAttributes.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry attr = (Map.Entry) it.next();
                            _setAttr((String) attr.getKey(), (String) attr.getValue());
                        }
                    } catch (Exception e) {
                        throw new BadParameter(e);
                    }
                } else {
                    // throw exception
                    throw new BadParameter("Missing attribute(s): "+missing.toString());                    
                }
            }
        }
    }

    private void _setAttr(String key, String value) throws NoSuccess {
        try {
            super.setAttribute(key, value);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
    private void _removeAttr(String key) throws NoSuccess {
        try {
            super.removeAttribute(key);
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
