package fr.in2p3.jsaga.impl.resource.description;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;

import org.apache.log4j.Logger;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.PermissionDeniedException;
import org.ogf.saga.error.TimeoutException;
import org.ogf.saga.resource.description.ResourceDescription;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Collection;
import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class AbstractResourceDescriptionImpl extends AbstractAttributesImpl implements ResourceDescription {
    
    private Logger m_logger = Logger.getLogger(AbstractResourceDescriptionImpl.class);
//    protected ResourceDescriptionAttributes m_attributes;
    private Collection<String> m_scalarAttributes;
    private Collection<String> m_vectorAttributes;
    
    /** constructor for ResourceFactory.createDescription() */
    public AbstractResourceDescriptionImpl() {
        super(null, true);      //isExtensible=true
//        m_attributes = new ResourceDescriptionAttributes(this);
        m_scalarAttributes = this.getScalarAttributes();
        m_vectorAttributes = this.getVectorAttributes();
    }

    protected Collection<String> getScalarAttributes() {
        Collection<String> c = new ArrayList<String>();
        c.add(ResourceDescription.TYPE);
        c.add(ResourceDescription.PLACEMENT);
        c.add(ResourceDescription.DYNAMIC);
        c.add(ResourceDescription.START);
        c.add(ResourceDescription.END);
        c.add(ResourceDescription.DURATION);
        return c;
    }

    protected Collection<String> getVectorAttributes() {
        Collection<String> c = new ArrayList<String>();
        c.add(ResourceDescription.TEMPLATE);
        return c;
    }
    /** constructor for ResourceManager.getTemplate() */
    public AbstractResourceDescriptionImpl(Properties properties) {
        super(null, true);      //isExtensible=true
        m_scalarAttributes = this.getScalarAttributes();
        m_vectorAttributes = this.getVectorAttributes();
        for (Entry<Object, Object> entrySet: properties.entrySet()) {
            try {
                if (entrySet.getValue() instanceof String) {
                    this.setAttribute((String)entrySet.getKey(), (String)entrySet.getValue());
                } else if (entrySet.getValue() instanceof String[]) {
                    this.setVectorAttribute((String)entrySet.getKey(), (String[])entrySet.getValue());
                }
            } catch (Exception e) {
                m_logger.error("Could not setAtrribute", e);
            }
        }
//        m_attributes = new ResourceDescriptionAttributes(this);
//        if (properties.containsKey(ResourceDescription.TEMPLATE)) {
//            if (properties.get(ResourceDescription.TEMPLATE) instanceof String) {
//                m_attributes.m_template.setObjects(new String[]{(String)properties.get(ResourceDescription.TEMPLATE)});
//            } else {
//                m_attributes.m_template.setObjects((String[])properties.get(ResourceDescription.TEMPLATE));
//            }
//        }
//        m_attributes.m_placement.setObject(properties.getProperty(ResourceDescription.PLACEMENT));
    }

    @Override
    public void setAttribute(String key, String value) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_vectorAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a vector attribute: "+key);
        }
        super.setAttribute(key, value);
    }

    @Override
    public String getAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_vectorAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a vector attribute: "+key);
        }
        return super.getAttribute(key);
    }

    @Override
    public void setVectorAttribute(String key, String[] values) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, BadParameterException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_scalarAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a scalar attribute: "+key);
        }
        super.setVectorAttribute(key, values);
    }

    @Override
    public String[] getVectorAttribute(String key) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, PermissionDeniedException, IncorrectStateException, DoesNotExistException, TimeoutException, NoSuccessException {
        if (m_scalarAttributes.contains(key)) {
            throw new IncorrectStateException("Attribute is a scalar attribute: "+key);
        }
        return super.getVectorAttribute(key);
    }
}
