package fr.in2p3.jsaga.impl.context;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.*;
import fr.in2p3.jsaga.engine.factories.SecurityAdaptorFactory;
import fr.in2p3.jsaga.engine.session.SessionConfiguration;
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
    public static final String URL_PREFIX = "UrlPrefix";
    public static final String BASE_URL_INCLUDES = "BaseUrlIncludes";
    public static final String BASE_URL_EXCLUDES = "BaseUrlExcludes";
    public static final String JOB_SERVICE_ATTRIBUTES = "JobServiceAttributes";
    public static final String DATA_SERVICE_ATTRIBUTES = "DataServiceAttributes";

    private static Logger s_logger = Logger.getLogger(ContextImpl.class);
    
    private ContextAttributes m_attributes;
    private SecurityAdaptor m_adaptor;
    private SecurityCredential m_credential;
    private WeakHashMap<JobServiceImpl,Object> m_jobServices;
    private SessionConfiguration m_config;
    private SecurityAdaptorFactory m_adaptorFactory;

    /** constructor */
    public ContextImpl(String type, SessionConfiguration config, SecurityAdaptorFactory adaptorFactory) throws IncorrectStateException, TimeoutException, NoSuccessException {
        super(null, true);  //not attached to a session, isExtensible=true
        m_attributes = new ContextAttributes(this);
        m_adaptor = null;
        m_credential = null;
        m_jobServices = new WeakHashMap<JobServiceImpl,Object>();
        m_config = config;
        m_adaptorFactory = adaptorFactory;
        if (type!=null && !type.equals("")) {
            try {
                this.setAttribute(Context.TYPE, type);
            }
            catch (IncorrectStateException e) {throw e;}
            catch (TimeoutException e) {throw e;}
            catch (NoSuccessException e) {throw e;}
            catch (SagaException e) {throw new NoSuccessException(e);}
        }
    }

    /** clone */
    public SagaObject clone() throws CloneNotSupportedException {
        ContextImpl clone = (ContextImpl) super.clone();
        clone.m_attributes = m_attributes;
        clone.m_adaptor = m_adaptor;
        clone.m_credential = m_credential;
        clone.m_jobServices = m_jobServices;
        clone.m_config = m_config;
        clone.m_adaptorFactory = m_adaptorFactory;
        return clone;
    }

    ////////////////////////// override some AbstractAttributesImpl methods //////////////////////////

    /** override super.setAttribute() */
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        try {
            m_attributes.getScalarAttribute(key).setValue(value);
        } catch (DoesNotExistException e) {
            super.setAttribute(key, value);
        }
        
        // instanciate adaptor
        if (Context.TYPE.equals(key)) {
            // instanciate
            m_adaptor = m_adaptorFactory.getSecurityAdaptor(value);

            // set PLUG-IN defaults
            Default[] defaults = m_adaptor.getDefaults(new HashMap());
            if (defaults != null) {
                for (int i=0; i<defaults.length; i++) {
                    if (defaults[i].getValue() != null) {
                        super.setAttribute(defaults[i].getName(), defaults[i].getValue());
                    }
                }
            }

            // set CONFIGURATION defaults (/jsaga-defaults/contexts)
            if (m_config != null) {
                m_config.setDefaultContext(this);
            }
        }

        // reset credential
        m_credential = null;
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // get attribute
        try {
            return m_attributes.getScalarAttribute(key).getValue();
        } catch (DoesNotExistException e) {
            // try to get from credential
            SecurityCredential credential = null;
            try{credential=this.getCredential();} catch(IncorrectStateException e2){/* ignore "Missing attribute" */}
            if (credential != null) {
                if (Context.USERID.equals(key)) {
                    try {
                        // try to get from credential
                        return credential.getUserID();
                    } catch (Exception e2) {
                        throw new NoSuccessException(e2);
                    }
                } else {
                	try{return credential.getAttribute(key);} catch(NotImplementedException e2){/* ignore "Unsupported attribute" */}
                }
            }
            // else try to get from parent class
            try {
                return super.getAttribute(key);
            } catch (DoesNotExistException dnee) {
                if (m_adaptor.getUsage().toString().contains(key)) {
                    throw dnee;
                } else if (Context.USERID.equals(key)) {
                    throw new IncorrectStateException("Attribute not yet initialized. Please first add context to a session.");
                } else {
                    throw new NoSuccessException("Attribute not supported for this adaptor (or not yet initialized)");
                }
            }
        }
    }

    /** override super.setVectorAttribute() */
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        // set attribute
        try {
            m_attributes.getVectorAttribute(key).setValues(values);
        } catch (DoesNotExistException e) {
            super.setVectorAttribute(key, values);
        }

        // reset credential
        m_credential = null;
    }

    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // get attribute
        try {
            return m_attributes.getVectorAttribute(key).getValues();
        } catch (DoesNotExistException e) {
            if (Context.USERID.equals(key)) {
                throw new IncorrectStateException("Operation not allowed on scalar attribute: "+key, this);
            } else {
                // try to get from parent class
                return super.getVectorAttribute(key);
            }
        }
    }

    private String getAttributeFromCredential(String key, SecurityCredential credential) throws NotImplementedException, DoesNotExistException, NoSuccessException {
        try {
            return credential.getAttribute(key);
        } catch (NotImplementedException e) {
            Usage usage = m_adaptor.getUsage();
            if (usage!=null && usage.getKeys().contains(key)) {
                throw new DoesNotExistException("Attribute not set: "+key);
            } else {
                throw new NotImplementedException("Attribute not supported: "+key);
            }
        }
    }

    ///////////////////////////////////////// implementation /////////////////////////////////////////

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
            // reset credential
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
                        super._getAttributesMap(), m_attributes.m_type.getValue());
            } catch (Exception e) {
                throw new NoSuccessException(e);
            }
        }
        // reset credential
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
    public synchronized Class getCredentialClass() throws BadParameterException {
        if (m_adaptor== null) {
            throw new BadParameterException("Attribute MUST be set before using context: "+Context.TYPE, this);
        }
        return m_adaptor.getSecurityCredentialClass();
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public synchronized SecurityCredential getCredential() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
        if (m_adaptor== null) {
            throw new IncorrectStateException("Attribute MUST be set before using context: "+Context.TYPE, this);
        }

        return m_credential;
    }

    public synchronized SecurityCredential createCredential() throws NotImplementedException, IncorrectStateException, TimeoutException, NoSuccessException {
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
        } catch (BadParameterException e) {
            throw new IncorrectStateException("Invalid attribute(s): ", e);
        }
        m_credential = m_adaptor.createSecurityCredential(
                matching, attributes, m_attributes.m_type.getValue());
        if (m_credential== null) {
            throw new NotImplementedException("[INTERNAL ERROR] Method createSecurityCredential should never return 'null'");
        }

        // reset the job services using this context
        Set<JobServiceImpl> jobServices = new HashSet<JobServiceImpl>();
        jobServices.addAll(m_jobServices.keySet());
        new Thread(new JobServiceReset(jobServices, m_credential)).start();
        return m_credential;
    }
    
    /**
     * This method is specific to JSAGA implementation.
     */
    public String getSchemeFromAlias(String alias) throws NotImplementedException, NoSuccessException {
        String urlPrefix;
        try{urlPrefix=m_attributes.m_urlPrefix.getValue()+"-";} catch(IncorrectStateException e){throw new NoSuccessException(e);}
        if (alias.startsWith(urlPrefix)) {
            return alias.substring(urlPrefix.length());
        } else {
            String scheme = m_attributes.m_baseUrlIncludes.getSchemeFromAlias(alias);
            if (scheme != null) {
                return scheme;
            } else {
                return alias;
            }
        }
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public Properties getServiceConfig(String serviceType, String scheme) {
    	if (JOB_SERVICE_ATTRIBUTES.equals(serviceType)) {
    		return m_attributes.m_jobServiceAttributes.getServiceConfig(scheme);
    	} else if (DATA_SERVICE_ATTRIBUTES.equals(serviceType)) {
    		return m_attributes.m_dataServiceAttributes.getServiceConfig(scheme);
    	} else {
    		return null;
    	}
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void throwIfConflictsWith(ContextImpl ref) throws NoSuccessException {
        try {
            m_attributes.m_baseUrlIncludes.throwIfConflictsWith(
                    m_attributes.m_urlPrefix.getValue(),
                    ref.m_attributes.m_urlPrefix.getValue(),
                    ref.m_attributes.m_baseUrlIncludes,
                    ref.m_attributes.m_baseUrlExcludes,
                    m_attributes.m_baseUrlExcludes);
        }
        catch (NoSuccessException e) {throw e;}
        catch (SagaException e) {throw new NoSuccessException(e);}
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public boolean matches(String url) {
        // returns false if matches an excluded pattern
        if (m_attributes.m_baseUrlExcludes.matches(url)) {
            return false;
        }
        // returns true if matches an included pattern
        return m_attributes.m_baseUrlIncludes.matches(url);
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public void setUrlPrefix(int position) throws NoSuccessException {
        try {
            if (m_attributes.m_urlPrefix.getValue() == null) {
                m_attributes.m_urlPrefix.setValue(m_attributes.m_type.getValue()+position);
            }
        }
        catch (NoSuccessException e) {throw e;}
        catch (SagaException e) {throw new NoSuccessException(e);}
    }

    /**
     * This method is specific to JSAGA implementation.
     */
    public synchronized void registerJobService(JobServiceImpl jobService) {
        m_jobServices.put(jobService, new Object());
    }

    /** This method is specific to JSAGA implementation. It should be used for debugging purpose only. */
    public String getUsage() {
        Usage usage = m_adaptor.getUsage();
        if (usage != null) {
            return usage.toString();
        }
        return null;
    }
    /** This method is specific to JSAGA implementation. It should be used for debugging purpose only. */
    public String getDefault(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        // do not try to get attribute from credential
        if (super.isVectorAttribute(key)) {
            return Arrays.toString(super.getVectorAttribute(key));
        } else {
            return super.getAttribute(key);
        }
    }
    /** This method is specific to JSAGA implementation. It should be used for debugging purpose only. */
    public String getMissings() throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, DoesNotExistException, IncorrectStateException, TimeoutException, NoSuccessException {
        Map<String,String> defaults = new HashMap<String,String>();
        for (String key : this.listAttributes()) {
            defaults.put(key, this.getDefault(key));
        }
        Usage usage = m_adaptor.getUsage();
        if (usage != null) {
            Usage missing = usage.getMissingValues(defaults);
            if (missing != null) {
                return missing.toString();
            }
        }
        return null;
    }
}
