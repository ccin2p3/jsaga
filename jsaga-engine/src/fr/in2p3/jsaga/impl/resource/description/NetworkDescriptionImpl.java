package fr.in2p3.jsaga.impl.resource.description;

import org.ogf.saga.resource.description.NetworkDescription;

import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class NetworkDescriptionImpl extends AbstractResourceDescriptionImpl implements NetworkDescription {
    /** constructor for ResourceFactory.createDescription() */
    public NetworkDescriptionImpl() {
        super();
    }

    /** constructor for ResourceManager.getTemplate() */
    public NetworkDescriptionImpl(Properties properties) {
        super(properties);
    }
}
