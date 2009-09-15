package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.*;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.factories.SecurityAdaptorBuilderFactory;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
    private static Logger s_logger = Logger.getLogger(ContextImpl.class);
    private ContextAttributes m_attributes;
    private SecurityAdaptorBuilder m_adaptorBuilder;
    private SecurityAdaptor m_adaptor;

    /** constructor */
    public ContextImpl(String type) throws NotImplementedException, IncorrectStateException, NoSuccessException {
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
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        ContextImpl clone = (ContextImpl) super.clone();
        clone.m_attributes = m_attributes;
        clone.m_adaptorBuilder = m_adaptorBuilder;
        clone.m_adaptor = m_adaptor;
        return clone;
    }

    ////////////////////////// override some AbstractAttributesImpl methods //////////////////////////

    /** override super.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        if (Context.TYPE.equals(key)) {
            m_attributes.m_type.setObject(value);
            m_adaptorBuilder = SecurityAdaptorBuilderFactory.getInstance().getSecurityAdaptorBuilder(value);
        } else {
            super.setAttribute(key, value);
        }
        // reset adaptor
        m_adaptor = null;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Context.TYPE.equals(key)) {
            return super.getAttribute(key);
        } else {
            // get adaptor
            SecurityAdaptor adaptor = this.getAdaptor();
            // get attribute
            if (Context.USERID.equals(key)) {
                try {
                    return adaptor.getUserID();
                } catch (Exception e) {
                    throw new NoSuccessException(e);
                }
            } else {
                String value = adaptor.getAttribute(key);
                if (value != null) {
                    return value;
                } else {
                    return super.getAttribute(key);
                }
            }
        }
    }

    /** override super.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        if (Context.TYPE.equals(key)) {
            throw new IncorrectStateException("Operation setVectorAttribute not allowed on scalar attribute: "+key, this);
        } else {
            super.setVectorAttribute(key, values);
        }
        // reset adaptor
        m_adaptor = null;
    }

    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (Context.TYPE.equals(key)) {
            return super.getVectorAttribute(key);
        } else {
            // get adaptor
            SecurityAdaptor adaptor = this.getAdaptor();
            // get attribute
            try {
                if (Context.USERID.equals(key)) {
                    throw new IncorrectStateException("Operation getVectorAttribute not allowed on scalar attribute: "+key, this);
                } else {
                    String value = adaptor.getAttribute(key);
                    if (value != null) {
                        throw new IncorrectStateException("Operation getVectorAttribute not allowed on scalar attribute: "+key, this);
                    } else {
                        return super.getVectorAttribute(key);
                    }
                }
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
    }

    ///////////////////////////////////////// implementation /////////////////////////////////////////

    public void setDefaults() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        if (m_attributes.m_type.getObject().equals("Unknown") || m_adaptorBuilder==null) {
            throw new IncorrectStateException("Attribute MUST be set before setting defaults: "+Context.TYPE, this);
        }

        // set default attributes with config
        fr.in2p3.jsaga.engine.schema.config.Context config = Configuration.getInstance().getConfigurations().getContextCfg().findContext(
                m_attributes.m_type.getObject());
        try {
            // set default attributes, from effective configuration
            for (int i=0; config!=null && i<config.getAttributeCount(); i++) {
                fr.in2p3.jsaga.engine.schema.config.Attribute attr = config.getAttribute(i);
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
            throw new NoSuccessException(e);
        }

        // reset adaptor
        m_adaptor = null;
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
            // reset adaptor
            m_adaptor = null;
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void destroy() throws IncorrectStateException, NoSuccessException {
        if (m_adaptorBuilder == null) {
            throw new IncorrectStateException("Attribute MUST be set before destroying context: "+Context.TYPE, this);
        }
        if (m_adaptorBuilder instanceof ExpirableSecurityAdaptorBuilder) {
            try {
                ((ExpirableSecurityAdaptorBuilder) m_adaptorBuilder).destroySecurityAdaptor(
                        super._getAttributesMap(), m_attributes.m_type.getObject());
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
        // reset adaptor
        m_adaptor = null;
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
        } else {
            out.println("Not yet initialized");
        }
        out.close();
        return stream.toString();
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public synchronized SecurityAdaptor getAdaptor() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        if (m_adaptorBuilder == null) {
            throw new IncorrectStateException("Attribute MUST be set before using context: "+Context.TYPE, this);
        }

        // create adaptor if needed
        if (m_adaptor == null) {
            Usage usage = m_adaptorBuilder.getUsage();
            Map attributes = super._getAttributesMap();
            int matching;
            try {
                matching = (usage!=null ? usage.getFirstMatchingUsage(attributes) : -1);
            } catch(DoesNotExistException e) {
                Usage missing = (usage!=null ? usage.getMissingValues(attributes) : null);
                if (missing != null) {
                    throw new IncorrectStateException("Missing attribute(s): "+missing.toString(), this);
                } else {
                    throw new NoSuccessException("[INTERNAL ERROR] Unexpected exception", this);
                }
            }
            m_adaptor = m_adaptorBuilder.createSecurityAdaptor(
                    matching, attributes, m_attributes.m_type.getObject());
            if (m_adaptor == null) {
                throw new NotImplementedException("[INTERNAL ERROR] Method createSecurityAdaptor should never return 'null'");
            }
        }
        return m_adaptor;
    }
}
