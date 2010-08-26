package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.*;
import fr.in2p3.jsaga.engine.config.Configuration;
import fr.in2p3.jsaga.engine.factories.SecurityAdaptorFactory;
import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import fr.in2p3.jsaga.impl.job.service.JobServiceImpl;
import org.apache.log4j.Logger;
import org.ogf.saga.SagaObject;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
    private static Logger s_logger = Logger.getLogger(ContextImpl.class);
    private ContextAttributes m_attributes;
    private SecurityAdaptor m_adaptor;
    private SecurityCredential m_credential;
    private WeakHashMap<JobServiceImpl,Map> m_jobServices;

    /** constructor */
    public ContextImpl(String type) throws NotImplementedException, IncorrectStateException, NoSuccessException {
        super(null, true);  //not attached to a session, isExtensible=true
        m_attributes = new ContextAttributes(this);
        if (type!=null && !type.equals("")) {
            m_attributes.m_type.setObject(type);
            m_adaptor = SecurityAdaptorFactory.getInstance().getSecurityAdaptor(type);
            this.setDefaults();
        } else {
            m_adaptor = null;
        }
        m_credential = null;
        m_jobServices = new WeakHashMap<JobServiceImpl,Map>();
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        ContextImpl clone = (ContextImpl) super.clone();
        clone.m_attributes = m_attributes;
        clone.m_adaptor = m_adaptor;
        clone.m_credential = m_credential;
        clone.m_jobServices = m_jobServices;
        return clone;
    }

    ////////////////////////// override some AbstractAttributesImpl methods //////////////////////////

    /** override super.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        try {
            m_attributes.getScalarAttribute(key).setObject(value);
        } catch (DoesNotExistException e) {
            super.setAttribute(key, value);
        }

        // instanciate adaptor
        if (Context.TYPE.equals(key)) {
            m_adaptor = SecurityAdaptorFactory.getInstance().getSecurityAdaptor(value);
        }
        // reset adaptor
        m_credential = null;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // get attribute
        try {
            return m_attributes.getScalarAttribute(key).getObject();
        } catch (DoesNotExistException e) {
            // try to get from credential
            SecurityCredential credential = this.getCredential();
            if (Context.USERID.equals(key)) {
                try {
                    return credential.getUserID();
                } catch (Exception e2) {
                    throw new NoSuccessException(e2);
                }
            } else {
                String value = credential.getAttribute(key);
                if (value != null) {
                    return value;
                }
            }
            // try to get from parent class
            return super.getAttribute(key);
        }
    }

    /** override super.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        try {
            m_attributes.getVectorAttribute(key).setObjects(values);
        } catch (DoesNotExistException e) {
            super.setVectorAttribute(key, values);
        }

        // reset adaptor
        m_credential = null;
    }

    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // get attribute
        try {
            return m_attributes.getVectorAttribute(key).getObjects();
        } catch (DoesNotExistException e) {
            // try to get from credential
            SecurityCredential credential = this.getCredential();
            try {
                if (Context.USERID.equals(key)) {
                    throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key, this);
                } else {
                    String value = credential.getAttribute(key);
                    if (value != null) {
                        throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key, this);
                    }
                }
            } catch (Exception e2) {
                throw new NoSuccessException(e2);
            }
            // try to get from parent class
            return super.getVectorAttribute(key);
        }
    }

    ///////////////////////////////////////// implementation /////////////////////////////////////////

    public void setDefaults() throws NotImplementedException, IncorrectStateException, NoSuccessException {
        if (m_attributes.m_type.getObject().equals("Unknown") || m_adaptor==null) {
            throw new IncorrectStateException("Attribute MUST be set before setting defaults: "+Context.TYPE, this);
        }

        // set default attributes with config
        fr.in2p3.jsaga.engine.schema.config.Context config = Configuration.getInstance().getConfigurations().getContextCfg().findContext(
                m_attributes.m_type.getObject());
        try {
            Set<String> defaultsToRemove = new HashSet<String>();

            // set default attributes, from effective configuration
            for (int i=0; config!=null && i<config.getAttributeCount(); i++) {
                fr.in2p3.jsaga.engine.schema.config.Attribute attr = config.getAttribute(i);
                if (! super._containsAttributeKey(attr.getName())) {
                    super.setAttribute(attr.getName(), attr.getValue());
                } else if (attr.getValue() == null) {
                    defaultsToRemove.add(attr.getName());
                }
            }

            // set default attributes, for which value depends on defined attributes
            Default[] defaults = m_adaptor.getDefaults(super._getAttributesMap());
            for (int i=0; defaults!=null && i<defaults.length; i++) {
                Default attr = defaults[i];
                if (! super._containsAttributeKey(attr.getName()) && ! defaultsToRemove.contains(attr.getName())) {
                    super.setAttribute(attr.getName(), attr.getValue());
                }
            }
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }

        // reset adaptor
        m_credential = null;
    }

    /**
     * @see org.ogf.saga.session.Session
     */
    public void close() {
        if (m_credential!= null) {
            try {
                m_credential.close();
            } catch (Exception e) {
                s_logger.warn("Failed to close security adaptor", e);
            }
            // reset adaptor
            m_credential = null;
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void destroy() throws IncorrectStateException, NoSuccessException {
        if (m_adaptor== null) {
            throw new IncorrectStateException("Attribute MUST be set before destroying context: "+Context.TYPE, this);
        }
        if (m_adaptor instanceof ExpirableSecurityAdaptor) {
            try {
                ((ExpirableSecurityAdaptor) m_adaptor).destroySecurityAdaptor(
                        super._getAttributesMap(), m_attributes.m_type.getObject());
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
        // reset adaptor
        m_credential = null;
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public String toString() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(stream);
        if (m_credential!= null) {
            try {
                m_credential.dump(out);
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
    public synchronized SecurityCredential getCredential() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor== null) {
            throw new IncorrectStateException("Attribute MUST be set before using context: "+Context.TYPE, this);
        }

        // create adaptor if needed
        if (m_credential== null) {
            Usage usage = m_adaptor.getUsage();
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
            m_credential = m_adaptor.createSecurityCredential(
                    matching, attributes, m_attributes.m_type.getObject());
            if (m_credential== null) {
                throw new NotImplementedException("[INTERNAL ERROR] Method createSecurityCredential should never return 'null'");
            }

            // reset the job services using this context
            Map<JobServiceImpl,Map> jobServices = new HashMap<JobServiceImpl,Map>();
            jobServices.putAll(m_jobServices);
            new Thread(new JobServiceReset(jobServices, m_credential)).start();
        }
        return m_credential;
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public synchronized void registerJobService(JobServiceImpl jobService, Map attributes) {
        m_jobServices.put(jobService, attributes);
    }
}
