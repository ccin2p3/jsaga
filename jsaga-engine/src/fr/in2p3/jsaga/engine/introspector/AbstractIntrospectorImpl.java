package fr.in2p3.jsaga.engine.introspector;

import fr.in2p3.jsaga.impl.attributes.AbstractAttributesImpl;
import fr.in2p3.jsaga.introspector.Introspector;
import org.ogf.saga.ObjectType;
import org.ogf.saga.error.*;

import java.util.HashSet;
import java.util.Set;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractIntrospectorImpl
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   28 août 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractIntrospectorImpl extends AbstractAttributesImpl implements Introspector {
    public AbstractIntrospectorImpl(String name) throws NoSuccess {
        super(null);
        super._addReadOnlyAttribute(Introspector.NAME, name);
        super._addReadOnlyAttribute(Introspector.CHILD_INTROSPECTOR_TYPE, this.getChildIntrospectorType());
    }

    public ObjectType getType() {
        return ObjectType.UNKNOWN;
    }

    /** override super.getVectorAttribute() */
    public String[] getVectorAttribute(String key) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, PermissionDenied, IncorrectState, DoesNotExist, Timeout, NoSuccess {
        if (this.getChildIntrospectorType().equalsIgnoreCase(key)) {
            return this.getChildIntrospectorKeys();
        } else {
            // recursive
            Set<String> result = new HashSet<String>();
            for (String childKey : this.getChildIntrospectorKeys()) {
                Introspector child = this.getChildIntrospector(childKey);
                for (String value : child.getVectorAttribute(key)) {
                    result.add(value);
                }
            }
            return result.toArray(new String[result.size()]);
        }
    }

    protected abstract String getChildIntrospectorType();
    protected abstract String[] getChildIntrospectorKeys() throws NoSuccess;
}
