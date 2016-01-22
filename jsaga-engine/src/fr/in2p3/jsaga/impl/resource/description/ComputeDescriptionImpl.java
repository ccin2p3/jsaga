package fr.in2p3.jsaga.impl.resource.description;

import org.ogf.saga.resource.description.ComputeDescription;

import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class ComputeDescriptionImpl extends AbstractResourceDescriptionImpl implements ComputeDescription {
    /** constructor for ResourceFactory.createDescription() */
    public ComputeDescriptionImpl() {
        super();
    }

    /** constructor for ResourceManager.getTemplate() */
    public ComputeDescriptionImpl(Properties properties) {
        super(properties);
    }
}
