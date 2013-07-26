package org.glite.security.authz;

import javax.security.auth.Subject;

/**
 * This class represents an attribute that is collected by a ServicePIP
 * implementation, and put into the javax.security.auth.Subject public or
 * private credentials, in order to later be read by ServicePDP implementations
 * It is recommended to subclass this class for faster lookups of attributes
 * of a specific type. The name of the PIPAttribute object should be unique
 * within a javax.security.auth.Subject instance to distinguish between
 * different attribute values.
 * @see ServicePDP
 * @see ServicePIP
 */
public class PIPAttribute {
    private String name;
    private Object value;

    /**
     * Constructor.
     * @param attributeName name of attribute
     * @param attributeValue value of attribute
     */
    public PIPAttribute(String attributeName, Object attributeValue) {
        this.name = attributeName;
        this.value = attributeValue;
    }
    /**
     * gets the value of the attribute.
     * @return atrtibute value
     */
    public Object getValue() {
        return this.value;
    }
    /**
     * gets the name of the attribute.
     * @return atrtibute name
     */
    public String getName() {
        return this.name;
    }
    /**
     * adds this attribute to the public credentials of specified subject.
     * @param subject subject to add attribute to
     */
    public void addPublic(Subject subject) {
        subject.getPublicCredentials().add(this);
    }
    /**
     * adds this attribute to the private credentials of specified subject.
     * @param subject subject to add attribute to
     */
    public void addPrivate(Subject subject) {
        subject.getPrivateCredentials().add(this);
    }
}
