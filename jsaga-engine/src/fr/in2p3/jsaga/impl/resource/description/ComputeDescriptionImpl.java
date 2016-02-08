package fr.in2p3.jsaga.impl.resource.description;

import org.apache.log4j.Logger;
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
        return c;
    }

    @Override
    protected Collection<String> getVectorAttributes() {
        Collection<String> c = super.getVectorAttributes();
        c.add(ComputeDescription.HOST_NAMES);
        return c;
    }
    
}
