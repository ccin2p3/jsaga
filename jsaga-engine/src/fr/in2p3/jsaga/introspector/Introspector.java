package fr.in2p3.jsaga.introspector;

import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   Introspector
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface Introspector extends Attributes {
    /** Attribute name: the name. */
    public static final String NAME = "Name";

    /** Attribute name: the type of child introspector */
    public static final String CHILD_INTROSPECTOR_TYPE = "ChildIntrospectorType";

    /** Vector attribute name: supported schemes. */
    public static final String SCHEME = "Scheme";

    /** Vector attribute name: candidate host patterns. */
    public static final String HOST_PATTERN = "HostPattern";

    /** Vector attribute name: candidate service configurations. */
    public static final String SERVICE = "Service";

    /** Vector attribute name: candidate security context instances. */
    public static final String CONTEXT = "Context";

    /**
     * Create introspector for sub-component.
     * @param key the sub-component key value.
     * @return the created introspector.
     */
    public Introspector getChildIntrospector(String key) throws NotImplementedException, DoesNotExistException, NoSuccessException;
}
