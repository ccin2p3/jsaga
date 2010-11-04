package org.ogf.saga.sd;

import java.util.List;

import org.ogf.saga.SagaObject;

import org.ogf.saga.error.AuthenticationFailedException;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.TimeoutException;

/**
 * <p>
 * Provides the entry point for service discovery. Apart from the constructor it
 * has one method: <code>listServices</code> which returns the list of
 * descriptions of services matching the specified filter strings.
 * </p>
 * <p>
 * An implementation SHOULD return the results in a random order if there is
 * more than one result to avoid any tendency to overload particular services
 * while leaving others idle.
 * </p>
 * <p>
 * There are three filter strings: <code>serviceFilter</code>,
 * <code>dataFilter</code> and <code>authzFilter</code> which act together
 * to restrict the set of services returned. Each of the filter strings uses
 * SQL92 syntax as if it were part of a <code>WHERE</code> clause acting to
 * select from a single table that includes columns as described below for that
 * filter type. SQL92 has been chosen because it is widely known and has the
 * desired expressive power. Multi-valued attributes are treated as a set of
 * values.
 * </p>
 * <p>
 * Three strings are used, rather than one, as this clarifies the description of
 * the functionality, avoids problems with key values being themselves existing
 * GLUE attributes, and facilitates implementation as it makes it impossible to
 * specify constraints that correlate, for example, service and authz
 * information. Only the following operators are permitted in expressions not
 * involving multi-valued attributes: <code>IN</code>, <code>LIKE</code>,
 * <code>AND</code>, <code>OR</code>, <code>NOT</code>, <code>=</code>,
 * <code>&gt;=</code>, <code>&gt;</code>, <code>&lt;=</code>,
 * <code>&lt;</code>, <code>&lt;&gt;</code> in addition to column names,
 * parentheses, column values as single quoted strings, numeric values and the
 * comma. For a multi-valued attribute, the name of the attribute MUST have the
 * keyword <code>ALL</code> or <code>ANY</code> immediately before it,
 * unless comparison with a set literal is intended. For each part of the
 * expression, the attribute name MUST precede the literal value. An
 * implementation SHOULD try to give an informative error message if the filter
 * string does not conform. It is, however, sufficient to report in which filter
 * string the syntax error was found.
 * </p>
 * <dl>
 * <dt>The <code>LIKE</code> operator matches string patterns:</dt>
 * <dd><code>'%xyz'</code> matches all entries with trailing xyz</dd>
 * <dd><code>'xyz%'</code> matches all entries with leading xyz </dd>
 * <dd><code>'%xyz%'</code> matches all entries with xyz being a substring</dd>
 * </dl>
 * <p>
 * The <code>ESCAPE</code> keyword can be used with <code>LIKE</code> in the
 * normal way.
 * </p>
 * <p>
 * Column names are not case sensitive but values are.
 * </p>
 * <p>
 * No use-case has been identified for the operators <code>&gt=</code>,
 * <code>&gt;</code>, <code>&lt;=</code>, <code>&gt;</code> to be
 * applied to strings. An Implementation wishing to support these comparison
 * operators on strings MUST select a collation sequence. Alternatively, an
 * implementation CAN treat all string comparisons as true, or reject them as
 * invalid SQL.
 * </p>
 * <h2>Service Filter</h2>
 * <p>
 * Column names in the <code>serviceFilter</code> are:
 * </p>
 * <dl>
 * <dt><code>Capabilities</code></dt>
 * <dd>identifiable aspects of functionality</dd>
 * <dt><code>ImplementationVersion</code></dt>
 * <dd>the version of the service implementation</dd>
 * <dt><code>Implementor</code></dt>
 * <dd>name of the organisation providing the implementation of the service</dd>
 * <dt><code>InterfaceVersion</code></dt>
 * <dd>the version of the service interface</dd>
 * <dt><code>Name</code></dt>
 * <dd>name of service (not necessarily unique)</dd>
 * <dt><code>RelatedServices</code></dt>
 * <dd>the uids of services related to the one being looked for</dd>
 * <dt><code>Site</code></dt>
 * <dd>name of site the service is running at</dd>
 * <dt><code>Type</code></dt>
 * <dd>type of service. This API does not restrict values of the service type --
 * it might be a DNS name, a URN or any other non-empty string.</dd>
 * <dt><code>Uid</code></dt>
 * <dd>unique identifier of service</dd>
 * <dt><code>Url</code></dt>
 * <dd>the endpoint to contact the service - will normally be used with the
 * LIKE operator</dd>
 * </dl>
 * <dl>
 * <dt>Some examples are:</dt>
 * <dd><code>ANY Capabilities = 'org.ogf.saga.service.job'</code></dd>
 * <dd><code>Site IN ('INFN-CNAF', 'RAL-LCG2')</code></dd>
 * <dd><code>Type = 'org.glite.ResourceBroker' AND Site LIKE '%.uk' AND Implementor = 'EGEE'</code></dd>
 * <dd><code>ANY RelatedServices = 'someServiceUID'</code></dd>
 * </dl>
 * <p>
 * Note the use of the <code>ANY</code> keyword in two of these examples as
|Capabilities| and |RelatedServices| are multi-valued.
 * </p>
 * <h2>Data Filter</h2>
 * <p>
 * Column names in the the <code>dataFilter</code> string are matched against
 * the service data key/value pairs. No keys are predefined by this
 * specification.
 * </p>
 * <p>
 * If values are specified as numeric values and not in single quotes, the
 * service data will be converted from string to numeric for comparison.
 * </p>
 * <p>
 * Data attributes may be multi-valued. If a <code>dataFilter</code> string
 * does not have the correct syntax to accept multi-valued attributes, and a
 * service has more than one value for an attribute mentioned in the filter,
 * that service MUST be rejected.
 * </p>
 * <dl>
 * <dt>Some examples are:</dt>
 * <dd><code>source = 'RAL-LCG2' OR destination = 'RAL-LCG2'</code></dd>
 * <dd><code>RunningJobs >= 1 AND RunningJobs <= 5</code></dd>
 * </dl>
 * <h2>Authz Filter</h2>
 * <p>
 * The set of column names in the <code>authzFilter</code> is not defined.
 * Instead the list below shows a possible set of names and how they might be
 * interpreted. Each of these column names could reasonably be related to an
 * authorization decision. Implementations MAY reuse the attribute names defined
 * for the {@link org.ogf.saga.context.Context org.ogf.saga.context.Contex}
 * class.
 * </p>
 * <dl>
 * <dt>Vo</dt>
 * <dd>virtual organization - will often be used with the IN operator</dd>
 * <dt>Dn</dt>
 * <dd>an X.509 ``distinguished name''</dd>
 * <dt>Group</dt>
 * <dd>a grouping of people within a Virtual Organization</dd>
 * <dt>Role</dt>
 * <dd>values might include ``Administrator'' or ``ProductionManager''</dd>
 * </dl>
 * <p>
 * It is expected that many of the attributes used in the
 * <code>authzFilter</code> will be multi-valued.
 * </p>
 * <dl>
 * <dt>Some examples, where <code>VO</code> is assumed to be multi-valued
 * are:</dt>
 * <dd><code>ANY Vo IN ('cms', 'atlas')</code></dd>
 * <dd><code>Vo = ('dteam')</code></dd>
 * </dl>
 * <p>
 * Note the use of the set constructor in both examples. Being a set,
 * ('aaa','bbbb') is of course the same as ('bbb', 'aaa').
 * </p>
 * <p>
 * The <code>listServices</code> method is overloaded: the last parameter the
 * <code>authzFilter</code> may be omitted. If it is omitted the authorization
 * filtering is performed on the contexts in the session. This is quite
 * different from including the <code>authzFilter</code> parameter with an
 * empty string which means that there is <b>no</b> authz filtering.
 * </p>
 */
public interface Discoverer extends SagaObject {

    /**
     * Returns the set of services that pass the set of specified filters, an
     * implicit <code>authzFilter</code> is constructed from the contexts of
     * the session. Note that this is different from an empty
     * <code>authzFilter</code>, as that would apply no authorization filter
     * at all.
     * 
     * @param serviceFilter
     *            a string containing the filter for filtering on the basic
     *            service and site attributes and on related services
     * @param dataFilter
     *            a string containing the filter for filtering on key/value
     *            pairs associated with the service
     * @return list of service descriptions, in a random order, matching the
     *         filter criteria
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws BadParameterException
     *             if any filter has an invalid syntax or if any filter uses
     *             invalid keys. However the <code>dataFilter</code> never
     *             signals invalid keys as there is no schema with permissible
     *             key names.
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    public List<ServiceDescription> listServices(String serviceFilter, String dataFilter)
            throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            NoSuccessException, TimeoutException;

    /**
     * Returns the set of services that pass the set of specified filters. A
     * service will only be included once in the returned list of services.
     * 
     * @param serviceFilter
     *            a string containing the filter for filtering on the basic
     *            service and site attributes and on related services
     * @param dataFilter
     *            a string containing the filter for filtering on key/value
     *            pairs associated with the service
     * @param authzFilter
     *            a string containing the filter for filtering on authorization
     *            information associated with the service
     * @return list of service descriptions, in a random order, matching the
     *         filter criteria
     * @throws AuthenticationFailedException
     *             if none of the available session contexts could successfully
     *             be used for authentication
     * @throws AuthorizationFailedException
     *             if none of the available contexts of the used session could
     *             be used for successful authorization. That error indicates
     *             that the resource could not be accessed at all, and not that
     *             an operation was not available due to restricted permissions.
     * @throws BadParameterException
     *             if any filter has an invalid syntax or if any filter uses
     *             invalid keys. However the <code>dataFilter</code> never
     *             signals invalid keys as there is no schema with permissible
     *             key names.
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws TimeoutException
     *             if a remote operation did not complete successfully because
     *             the network communication or the remote service timed out
     */
    public List<ServiceDescription> listServices(String serviceFilter, String dataFilter, String authzFilter)
            throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException,
            NoSuccessException, TimeoutException;

}
