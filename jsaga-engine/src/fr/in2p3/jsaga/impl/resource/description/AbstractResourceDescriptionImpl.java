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

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class AbstractResourceDescriptionImpl extends AbstractAttributesImpl implements ResourceDescription {
    
    private Logger m_logger = Logger.getLogger(AbstractResourceDescriptionImpl.class);
    
    /** constructor for ResourceFactory.createDescription() */
    public AbstractResourceDescriptionImpl() {
        super(null, true);      //isExtensible=true
    }

    /** constructor for ResourceManager.getTemplate() */
    public AbstractResourceDescriptionImpl(Properties properties) {
        super(null, true);      //isExtensible=true
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
    }
}
