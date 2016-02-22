package fr.in2p3.jsaga.impl.resource.description;

import org.apache.log4j.Logger;
import org.ogf.saga.resource.description.StorageDescription;

import java.util.Collection;
import java.util.Properties;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************/
public class StorageDescriptionImpl extends AbstractResourceDescriptionImpl implements StorageDescription {
    private Logger m_logger = Logger.getLogger(StorageDescriptionImpl.class);

    /** constructor for ResourceFactory.createDescription() */
    public StorageDescriptionImpl() {
        super();
    }

    /** constructor for ResourceManager.getTemplate() */
    public StorageDescriptionImpl(Properties properties) {
        super(properties);
    }

    @Override
    protected Collection<String> getScalarAttributes() {
        Collection<String> c = super.getScalarAttributes();
        c.add(StorageDescription.ACCESS);
        c.add(StorageDescription.SIZE);
        return c;
    }

}
