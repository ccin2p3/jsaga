package org.ogf.saga.sd;

import java.util.Set;

import org.ogf.saga.SagaObject;
import org.ogf.saga.attributes.Attributes;
import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/**
 * <p>
 * Read access to the top level data of the service and a means to navigate to
 * related services. This class implements the
 * {@link org.ogf.saga.attributes.Attributes org.ogf.saga.attributes.Attributes}
 * interface and offers getter methods to obtain details of that service. The
 * attributes are based on those found in GLUE. They are:
 * </p>
 * <dl>
 * <dt><code>url</code></dt>
 * <dd>url to contact the service. The {@link #getUrl getUrl} method obtains
 * the same information.
 * <dt><code>type</code></dt>
 * <dd>type of service. This field is not an empty string.</dd>
 * <dt><code>uid</code></dt>
 * <dd>unique identifier of service. This field is not an empty string.</dd>
 * <dt><code>site</code></dt>
 * <dd>name of site. This field is not an empty string.</dd>
 * <dt><code>name</code></dt>
 * <dd>name of service - not necessarily unique. This field is not an empty
 * string.</dd>
 * <dt><code>implementor</code></dt>
 * <dd>name of the organisation providing the implementation of the service.
 * This field is not an empty string.</dd>
 * <dt><code>relatedServices</code></dt>
 * <dd>uids of related services. This returns the uids of the related services.
 * This is unlike the method {@link #getRelatedServices getRelatedServices}
 * which returns an array of {@link ServiceDescription}s.</dd>
 * </dl>
 * <p>
 * This class has no CONSTRUCTOR as objects of this type are created only by
 * other objects in the service discovery API.
 * </p>
 */
public interface ServiceDescription extends SagaObject, Attributes {

    /** Attribute name, url to contact the service. */
    public static final String URL = "url";

    /** Attribute name, type of service. */
    public static final String TYPE = "type";

    /** Attribute name, unique identifier of service. */
    public static final String UID = "uid";

    /** Attribute name, name of site. */
    public static final String SITE = "site";

    /** Attribute name, name of service - not necessarily unique. */
    public static final String NAME = "name";

    /**
     * Attribute name, name of the organisation providing the implementation of
     * the service.
     */
    public static final String IMPLEMENTOR = "implementor";

    /** Attribute name, uids of related services. */
    public static final String RELATED_SERVICES = "relatedServices";

    /**
     * Returns the <code>URL</code> to contact the service. The
     * <code>URL</code> may also be obtained using the
     * <code>org.ogf.saga.attributes.Attributes</code> interface.
     * 
     * @return a string containing the URL to contact this service
     */
    public String getUrl();

    /**
     * Returns the set of related services. Alternatively, the
     * <code>org.ogf.saga.attributes.Attributes</code> interface may be used
     * to get the uids of the related services.
     * 
     * @return a set of related services. This may be an empty set.
     * @throws AuthorizationFailedException
     *                 if none of the available contexts of the used session
     *                 could be used for successful authorization. That error
     *                 indicates that the resource could not be accessed at all,
     *                 and not that an operation was not available due to
     *                 restricted permissions.
     * @throws AuthenticationFailedException
     *                 if none of the available session contexts could
     *                 successfully be used for authentication
     * @throws TimeoutException
     *                 if a remote operation did not complete successfully
     *                 because the network communication or the remote service
     *                 timed out
     * @throws NoSuccessException
     *                 if no result can be returned because of information
     *                 system or other internal problems
     */
    public Set<ServiceDescription> getRelatedServices()
	    throws AuthorizationFailedException, AuthenticationFailedException,
	    TimeoutException, NoSuccessException;

    /**
     * Returns a <code>ServiceData</code> object with the service data
     * key/value pairs.
     * 
     * @return the service data for this service. This may be empty, i.e.has no
     *         attributes at all.
     */
    public ServiceData getData();

}
