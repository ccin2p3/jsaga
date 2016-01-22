package fr.in2p3.jsaga.impl.resource.description;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import org.ogf.saga.resource.description.ResourceDescription;

import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class AbstractResourceDescriptionImpl extends AbstractAttributesImpl implements ResourceDescription {
    /** constructor for ResourceFactory.createDescription() */
    public AbstractResourceDescriptionImpl() {
        super(null, true);      //isExtensible=true
    }

    /** constructor for ResourceManager.getTemplate() */
    public AbstractResourceDescriptionImpl(Properties properties) {
        super(null, true);      //isExtensible=true

        //TODO: fill this instance with properties
    }
}
