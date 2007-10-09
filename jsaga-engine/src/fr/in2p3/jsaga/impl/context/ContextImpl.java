package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.*;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.factories.SecurityAdaptorBuilderFactory;
import fr.in2p3.jsaga.engine.schema.config.ContextInstance;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogf.saga.ObjectType;
import org.ogf.saga.SagaBase;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Exception;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ContextImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   17 sept. 2007
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
    public ContextImpl() throws NoSuccess {
        super(null, true);  //not attached to a session, isExtensible=true
        this._setAttr("Type", "Unknown");
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

    public ObjectType getType() {
        return ObjectType.CONTEXT;
    }

    ////////////////////////// override some AbstractAttributesImpl methods //////////////////////////

    /** override super.setAttribute() */
    public String setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        if (!this.isInitialized(key)) {
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(value);
        } else if (key.equals("Type")) {
            throw new IncorrectState("Not allowed to change the type of context: "+ m_adaptorBuilder.getType(), this);
        }
        return super.setAttribute(key, value);
    }
    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        this.isInitialized(key);
        return super.getAttribute(key);
    }
    /** override super.setVectorAttribute() */
    public String[] setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        if (!this.isInitialized(key)) {
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(values!=null && values.length>0 ? values[0] : null);
        } else if (key.equals("Type")) {
            throw new IncorrectState("Not allowed to change the type of context: "+ m_adaptorBuilder.getType(), this);
        }
        return super.setVectorAttribute(key, values);
    }
    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, ReadOnly, DoesNotExist, Timeout, NoSuccess {
        this.isInitialized(key);
        return super.getVectorAttribute(key);
    }

    ///////////////////////////////////////// implementation /////////////////////////////////////////

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
            initAdaptorBuilder.initBuilder(super._getAttributesMap(), this.getContextId());
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void destroy() throws IncorrectState, NoSuccess {
        if (m_adaptorBuilder == null) {
            throw new IncorrectState("Please set context type prior to invoking any other operation", this);
        }
        if (!(m_adaptorBuilder instanceof InitializableSecurityAdaptorBuilder)) {
            return; //ignore
        }
        InitializableSecurityAdaptorBuilder initAdaptorBuilder = (InitializableSecurityAdaptorBuilder) m_adaptorBuilder;
        try {
            initAdaptorBuilder.destroyBuilder(super._getAttributesMap(), this.getContextId());
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
        if (m_adaptor == null) {
            throw new NoSuccess("Bad adaptor: method createSecurityAdaptor should never return null");
        }
        return m_adaptor;
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(stream);

        // title
        Map map;
        try {
            map = super._getAttributesMap();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception: "+e.getClass().getName());
        }
        String type = (String) map.get("Type");
        String indice = (String) map.get("Indice");
        String name = (String) map.get("Name");
        out.println(
                (type!=null ? type : "???") +
                (indice!=null ? "["+indice+"]" : "") +
                (name!=null ? ": "+name : ""));

        // content
        try {
            if (m_adaptor == null) {
                m_adaptor = m_adaptorBuilder.createSecurityAdaptor(super._getAttributesMap());
            }
            m_adaptor.dump(out);
        } catch (BadParameter e) {
            out.println("  Not initialised: ["+e.getMessage()+"]");
        } catch (Exception e) {
            e.printStackTrace(out);
        }

        out.close();
        return stream.toString();
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public String getContextType() throws IncorrectState, NotImplemented {
        return super._getOptionalAttribute("Type");
    }

    /////////////////////////////////////// private methods ///////////////////////////////////////

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
                    try {
                        Map newAttributes = new HashMap();
                        missing.promptForValues(newAttributes, this.getContextId());
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

    private String getContextId() throws NotImplemented, IncorrectState {
        return (super._getOptionalAttribute("Name")!=null
                ? super._getOptionalAttribute("Name")
                : super._getOptionalAttribute("Type")+"["+ super._getOptionalAttribute("Indice")+"]");
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
