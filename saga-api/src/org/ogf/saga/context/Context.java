package org.ogf.saga.context;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.error.TimeoutException;

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

    /**
     * Sets default attribute values for this context type, based on all
     * non-empty attributes.
     * @exception NotImplementedException
     *      is thrown if the implementation does not provide an
     *      implementation of this method.
     * @exception IncorrectStateException
     *      is thrown if the <code>Type</code> attribute has an empty value.
     * @exception TimeoutException
     *      is thrown when a remote operation did not complete successfully
     *      because the network communication or the remote service timed
     *      out.
     * @exception NoSuccessException
     *      is thrown if the implementation cannot create valid
     *      default values based on the available information.
     */
    public void setDefaults() throws NotImplementedException,
            IncorrectStateException, TimeoutException, NoSuccessException;
}
