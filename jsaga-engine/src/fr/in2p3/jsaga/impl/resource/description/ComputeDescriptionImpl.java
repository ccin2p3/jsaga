package fr.in2p3.jsaga.impl.resource.description;

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
import org.ogf.saga.resource.description.ComputeDescription;
import org.ogf.saga.resource.description.ResourceDescription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ComputeDescriptionImpl extends AbstractResourceDescriptionImpl implements ComputeDescription {
    
    private Logger m_logger = Logger.getLogger(ComputeDescriptionImpl.class);
    public final static String DEFAULT_SIZE = "1";
    public final static String DEFAULT_OS = "ANY";
    public final static String DEFAULT_ARCH = "ANY";
    public final static String DEFAULT_ADMINUSER = "root";
    
    /**
     * The admin username on the compute resource
     * This attribute is outside the SAGA specification.
     */
    public final static String ADMINUSER = "AdminUser";
    
    /** constructor for ResourceFactory.createDescription() */
    public ComputeDescriptionImpl() {
        super();
        try {
            this.setAttribute(ComputeDescription.SIZE, DEFAULT_SIZE);
        } catch (Exception e) {
            m_logger.error("Could not set attribute " + ComputeDescription.SIZE, e);
        }
        try {
            this.setAttribute(ComputeDescription.MACHINE_ARCH, DEFAULT_ARCH);
        } catch (Exception e) {
            m_logger.error("Could not set attribute " + ComputeDescription.MACHINE_ARCH, e);
        }
        try {
            this.setAttribute(ComputeDescription.MACHINE_OS, DEFAULT_OS);
        } catch (Exception e) {
            m_logger.error("Could not set attribute " + ComputeDescription.MACHINE_OS, e);
        }
        try {
            this.setAttribute(ADMINUSER, DEFAULT_ADMINUSER);
        } catch (Exception e) {
            m_logger.error("Could not set attribute " + ADMINUSER, e);
        }
    }

    /** constructor for ResourceManager.getTemplate() */
    public ComputeDescriptionImpl(Properties properties) {
        super(properties);
    }
    
    @Override
    protected Collection<String> getScalarAttributes() {
        Collection<String> c = super.getScalarAttributes();
        c.add(ComputeDescription.MACHINE_ARCH);
        c.add(ComputeDescription.MACHINE_OS);
        c.add(ComputeDescription.ACCESS);
        c.add(ComputeDescription.SIZE);
        c.add(ComputeDescription.MEMORY);
        c.add(ADMINUSER);
        return c;
    }

    @Override
    protected Collection<String> getVectorAttributes() {
        Collection<String> c = super.getVectorAttributes();
        c.add(ComputeDescription.HOST_NAMES);
        return c;
    }
    
}
