package org.ogf.saga.context;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;

/**
 * A <code>Context</code> provides the functionality of a security information
 * container.
 */
public interface Context extends SagaObject, Attributes {
    /** Attribute name: type of context. */
    public static final String TYPE = "Type";

    /** Attribute name: server which manages the context. */
    public static final String SERVER = "Server";

    /** Attribute name: Location of certificates and CA signatures. */
    public static final String CERTREPOSITORY = "CertRepository";

    /** Attribute name: Location of an existing certificate proxy to be used. */
    public static final String USERPROXY = "UserProxy";

    /** Attribute name: Location of a user certificate to be used. */
    public static final String USERCERT = "UserCert";

    /** Attribute name: Location of a user key to use. */
    public static final String USERKEY = "UserKey";

    /** Attribute name: User ID or user name to use. */
    public static final String USERID = "UserID";

    /** Attribute name: Password to use. */
    public static final String USERPASS = "UserPass";

    /** Attribute name: The VO the context belongs to. */
    public static final String USERVO = "UserVO";

    /** Attribute name: Time up to which this context is valid. */
    public static final String LIFETIME = "LifeTime";

    /**
     * Attribute name: User ID for a remote user, who is identified by this
     * context. (ReadOnly)
     */
    public static final String REMOTEID = "RemoteID";

    /**
     * Attribute name: The hostname where the connection originates which is
     * identified by this context. (ReadOnly)
     */
    public static final String REMOTEHOST = "RemoteHost";

    /**
     * Attribute name: the port used for the connection which is identified by
     * this context. (ReadOnly)
     */
    public static final String REMOTEPORT = "RemotePort";

}
