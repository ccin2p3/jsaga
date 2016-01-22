package fr.in2p3.jsaga.impl.resource.description;

import org.ogf.saga.resource.description.StorageDescription;

import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class StorageDescriptionImpl extends AbstractResourceDescriptionImpl implements StorageDescription {
    /** constructor for ResourceFactory.createDescription() */
    public StorageDescriptionImpl() {
        super();
    }

    /** constructor for ResourceManager.getTemplate() */
    public StorageDescriptionImpl(Properties properties) {
        super(properties);
    }
}
