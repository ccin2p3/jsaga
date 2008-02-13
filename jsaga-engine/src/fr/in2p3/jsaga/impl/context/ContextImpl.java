package fr.in2p3.jsaga.impl.context;

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
import org.ogf.saga.SagaObject;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Exception;
import java.util.Map;

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
    /** Context attribute (deviation from SAGA specification) */
    public static final String NAME = "Name";
    /** Context attribute (deviation from SAGA specification) */
    public static final String INDICE = "Indice";
    
    private static Log s_logger = LogFactory.getLog(ContextImpl.class);
    private ContextAttributes m_attributes;
    private SecurityAdaptorBuilder m_adaptorBuilder;
    private SecurityAdaptor m_adaptor;
    private Exception m_exception;

    /** constructor */
    public ContextImpl(String type) throws NotImplemented, IncorrectState, NoSuccess {
        super(null, true);  //not attached to a session, isExtensible=true
        m_attributes = new ContextAttributes(this);
        if (type!=null && !type.equals("")) {
            m_attributes.m_type.setObject(type);
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(type);
            this.setDefaults();
        } else {
            m_adaptorBuilder = null;
        }
        m_adaptor = null;
        m_exception = null;
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        ContextImpl clone = (ContextImpl) super.clone();
        clone.m_attributes = m_attributes;
        clone.m_adaptorBuilder = m_adaptorBuilder;
        clone.m_adaptor = m_adaptor;
        clone.m_exception = m_exception;
        return clone;
    }

    public ObjectType getType() {
        return ObjectType.CONTEXT;
    }

    ////////////////////////// override some AbstractAttributesImpl methods //////////////////////////

    /** override super.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        // set attribute
        if (Context.TYPE.equals(key)) {
            m_attributes.m_type.setObject(value);
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(value);
            m_adaptor = null;
        } else if (ContextImpl.INDICE.equals(key)) {
            m_attributes.m_indice.setObject(new Integer(value));
        } else if (ContextImpl.NAME.equals(key)) {
            m_attributes.m_name.setObject(value);
        } else {
            super.setAttribute(key, value);
        }
        // build adaptor
        m_adaptor = this.buildAdaptor();
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (Context.TYPE.equals(key) || ContextImpl.INDICE.equals(key) || ContextImpl.NAME.equals(key)) {
            return super.getAttribute(key);
        } else if (m_adaptor != null) {
            // get attribute
            if (Context.USERID.equals(key)) {
                try {
                    return m_adaptor.getUserID();
                } catch (Exception e) {
                    throw new NoSuccess(e);
                }
            } else {
                String value = m_adaptor.getAttribute(key);
                if (value != null) {
                    return value;
                } else {
                    return super.getAttribute(key);
                }
            }
        } else {
            // throw exception
            this.throwException();
            throw new NoSuccess("INTERNAL ERROR: unexpected exception");
        }
    }

    /** override super.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, BadParameter, DoesNotExist, Timeout, NoSuccess {
        // set attribute
        if (Context.TYPE.equals(key) || ContextImpl.INDICE.equals(key) || ContextImpl.NAME.equals(key)) {
            throw new IncorrectState("Operation setVectorAttribute not allowed on scalar attribute: "+key, this);
        } else {
            super.setVectorAttribute(key, values);
        }
        // build adaptor
        m_adaptor = this.buildAdaptor();
    }

    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (Context.TYPE.equals(key) || ContextImpl.INDICE.equals(key) || ContextImpl.NAME.equals(key)) {
            return super.getVectorAttribute(key);
        } else if (m_adaptor != null) {
            // get attribute
            try {
                if (Context.USERID.equals(key)) {
                    throw new IncorrectState("Operation getVectorAttribute not allowed on scalar attribute: "+key, this);
                } else {
                    String value = m_adaptor.getAttribute(key);
                    if (value != null) {
                        throw new IncorrectState("Operation getVectorAttribute not allowed on scalar attribute: "+key, this);
                    } else {
                        return super.getVectorAttribute(key);
                    }
                }
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
        } else {
            // throw exception
            this.throwException();
            throw new NoSuccess("INTERNAL ERROR: unexpected exception", this);
        }
    }

    ///////////////////////////////////////// implementation /////////////////////////////////////////

    public void setDefaults() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_attributes.m_type.getObject().equals("Unknown") || m_adaptorBuilder==null) {
            throw new IncorrectState("Attribute MUST be set prior to setting defaults: "+Context.TYPE, this);
        }

        // set INDICE/NAME attributes
        ContextInstance xmlInstance = Configuration.getInstance().getConfigurations().getContextCfg().findContextInstance(
                m_attributes.m_type.getObject(),
                m_attributes.m_indice.getObject());
        if (xmlInstance.hasIndice()) {
            m_attributes.m_indice.setObject(xmlInstance.getIndice());
        }
        if (xmlInstance.getName() != null) {
            m_attributes.m_name.setObject(xmlInstance.getName());
        }

        // set other default attributes
        try {
            // set default attributes, from effective configuration
            for (int i=0; xmlInstance!=null && i<xmlInstance.getAttributeCount(); i++) {
                fr.in2p3.jsaga.engine.schema.config.Attribute attr = xmlInstance.getAttribute(i);
                if (! super._containsAttributeKey(attr.getName())) {
                    super.setAttribute(attr.getName(), attr.getValue());
                } else if (attr.getValue() == null) {
                    super.removeAttribute(attr.getName());
                }
            }

            // set default attributes, for which value depends on defined attributes
            Default[] defaults = m_adaptorBuilder.getDefaults(super._getAttributesMap());
            for (int i=0; defaults!=null && i<defaults.length; i++) {
                Default attr = defaults[i];
                if (! super._containsAttributeKey(attr.getName())) {
                    super.setAttribute(attr.getName(), attr.getValue());
                }
            }
        } catch (Exception e) {
            throw new NoSuccess(e);
        }

        // build adaptor
        m_adaptor = this.buildAdaptor();
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public SecurityAdaptor getAdaptor() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_adaptor == null) {
            m_adaptor = this.buildAdaptor();
        }
        return m_adaptor;
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
    public void destroy() throws IncorrectState, NoSuccess {
        if (m_adaptorBuilder == null) {
            throw new IncorrectState("Attribute MUST be set prior to destroying context: "+Context.TYPE, this);
        }
        if (m_adaptorBuilder instanceof ExpirableSecurityAdaptorBuilder) {
            try {
                ((ExpirableSecurityAdaptorBuilder) m_adaptorBuilder).destroySecurityAdaptor(super._getAttributesMap(), this.getContextId());
            } catch (Exception e) {
                throw new NoSuccess(e);
            }
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(stream);
        if (m_adaptor != null) {
            try {
                m_adaptor.dump(out);
            } catch (Exception e) {
                e.printStackTrace(out);
            }
        } else if (m_exception != null) {
            out.println("  Context not initialized ["+m_exception.getMessage()+"]");
        } else {
            out.println("  Context not initialized");
        }
        out.close();
        return stream.toString();
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public String getContextId() {
        return (m_attributes.m_name.getObject()!=null
                ? m_attributes.m_name.getObject()
                : m_attributes.m_type.getObject()+"["+m_attributes.m_indice.getObject()+"]");
    }

    /////////////////////////////////////// private methods ///////////////////////////////////////

    private SecurityAdaptor buildAdaptor() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_adaptorBuilder != null) {
            Usage usage = m_adaptorBuilder.getUsage();
            Map attributes = super._getAttributesMap();
            try {
                // getFirstMatchingUsage will throw DoesNotExist exception if no usage matches attributes
                int matching = (usage!=null ? m_adaptorBuilder.getUsage().getFirstMatchingUsage(attributes) : -1);
                // createSecurityAdaptor will throw IncorrectState exception if the context found is not of expected type
                return m_adaptorBuilder.createSecurityAdaptor(matching, attributes, this.getContextId());
            } catch(DoesNotExist e) {
                // no usage matches attributes
                m_exception = e;
                return null;
            } catch(IncorrectState e) {
                // context found is not expected type
                m_exception = e;
                return null;
            }
        } else {
            // attribute Type is not set yet
            m_exception = new IncorrectState("Attribute not found: Type");
            return null;
        }
    }

    private void throwException() throws NotImplemented, IncorrectState, NoSuccess {
        if (m_adaptorBuilder == null) {
            throw new IncorrectState("Attribute MUST be set prior to getting attribute: "+Context.TYPE, this);
        }
        if (m_adaptor == null) {
            Usage usage = m_adaptorBuilder.getUsage();
            if (usage != null) {
                Usage missing = m_adaptorBuilder.getUsage().getMissingValues(super._getAttributesMap());
                if (missing != null) {
                    throw new IncorrectState("Missing attribute(s): "+missing.toString(), this);
                }
            }
            if (m_exception != null) {
                try {
                    throw m_exception;
                } catch(NotImplemented e) {
                    throw e;
                } catch(IncorrectState e) {
                    throw e;
                } catch(NoSuccess e) {
                    throw e;
                } catch (Exception e) {
                    throw new NoSuccess(m_exception);
                }
            } else {
                throw new NoSuccess("Not initialized", this);
            }
        }
    }
}
