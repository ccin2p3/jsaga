package org.ogf.saga.url;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.session.Session;

/**
 * The <code>URL</code> interface provides methods to access or set
 * the individual parts of an URL, and to convert strings to URLs and
 * vice versa.
 */
public interface URL extends SagaObject {

    /**
     * Replaces the current value of the URL with the specified value.
     * 
     * @param url
     *            the string.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setString(String url) throws BadParameterException;
    
    /**
     * Replaces the current value of the URL with "".
     * 
     * @exception BadParameterException
     *            is thrown by {@link #setString(String)}.
     */
    public void setString() throws BadParameterException;

    /**
     * Returns this URL as a string. The result may contain non-escaped characters,
     * so may not be suitable for creating a new URL object. For that, you have to
     * use {@link #getEscaped()}.
     * 
     * @return the string.
     */
    public String getString();


    /**
     * Returns this URL as a string, with escapes added where needed to parse the
     * result as an URL.
     * 
     * @return the string.
     */
    public String getEscaped();
    
    /**
     * Returns the fragment part of this URL.
     * 
     * @return the fragment.
     */
    public String getFragment();

    /**
     * Sets the fragment part of this URL to the specified parameter.
     * 
     * @param fragment
     *            the fragment.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setFragment(String fragment) throws BadParameterException;
        
    /**
     * Sets the fragment part of this URL to "".
     * @exception BadParameterException
     *                is thrown by {@link #setFragment(String)}.
     */
    public void setFragment() throws BadParameterException;

    /**
     * Returns the host part of this URL.
     * 
     * @return the host.
     */
    public String getHost();

    /**
     * Sets the host part of this URL to the specified parameter.
     * 
     * @param host
     *            the host.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setHost(String host) throws BadParameterException;
    
    /**
     * Sets the host part of this URL to "".
     * @exception BadParameterException
     *            is thrown by {@link #setHost(String)}.
     */
    public void setHost() throws BadParameterException;
    
    /**
     * Returns the path part of this URL.
     * 
     * @return the path.
     */
    public String getPath();

    /**
     * Sets the path part of this URL to the specified parameter.
     * 
     * @param path
     *            the path.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the path.
     */
    public void setPath(String path) throws BadParameterException;
    
    
    /**
     * Sets the path part of this URL to "".
     * @exception BadParameterException
     *            is thrown by {@link #setPath(String)}.
     */
    public void setPath() throws BadParameterException;

    /**
     * Returns the port number of this URL.
     * 
     * @return the port number.
     */
    public int getPort();

    /**
     * Sets the port number of this URL to the specified parameter.
     * 
     * @param port
     *            the port number.
     * @exception BadParameterException
     *            is thrown when there is an error in the parameter.
     */
    public void setPort(int port) throws BadParameterException;


    /**
     * Sets the port number of this URL to -1.
     * @exception BadParameterException
     *            is thrown by {@link #setPort()}.
     */
    public void setPort() throws BadParameterException;
    
    /**
     * Returns the query part from this URL.
     * 
     * @return the query.
     */
    public String getQuery();

    /**
     * Sets the query part of this URL to the specified parameter.
     * 
     * @param query
     *            the query.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setQuery(String query) throws BadParameterException;
    
    /**
     * Sets the query part of this URL to "".
     * @exception BadParameterException
     *            is thrown by {@link #setQuery(String)}.
     */
    public void setQuery() throws BadParameterException;

    /**
     * Returns the scheme part from this URL.
     * 
     * @return the scheme.
     */
    public String getScheme();

    /**
     * Sets the scheme part of this URL to the specified parameter.
     * 
     * @param scheme
     *            the scheme.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setScheme(String scheme) throws BadParameterException;
    

    /**
     * Sets the scheme part of this URL to the specified parameter.
     * @exception BadParameterException
     *            is thrown by {@link #setScheme(String)}.
     */
    public void setScheme() throws BadParameterException;

    /**
     * Returns the userinfo part from this URL.
     * 
     * @return the userinfo.
     */
    public String getUserInfo();

    /**
     * Sets the user info part of this URL to the specified parameter.
     * 
     * @param userInfo
     *            the userinfo.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the parameter.
     */
    public void setUserInfo(String userInfo) throws BadParameterException;
    

    /**
     * Sets the user info part of this URL to "".
     * @exception BadParameterException
     *            is thrown by {@link #setUserInfo(String)}.
     */
    public void setUserInfo() throws BadParameterException;

    /**
     * Returns a new URL with the scheme part replaced.
     * 
     * @param scheme
     *            the new scheme.
     * @return the new URL.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the new URL.
     * @exception NoSuccessException
     *            is thrown when the scheme is supported, but the URL
     *            cannot be translated to the scheme.
     */
    public URL translate(String scheme) throws BadParameterException, NoSuccessException;


    /**
     * Returns a new URL with the scheme part replaced.
     * 
     * @param session
     *            session to be used for possibly-needed back-end communication.
     * @param scheme
     *            the new scheme.
     * @return the new URL.
     * @exception BadParameterException
     *            is thrown when there is a syntax error in the new URL.
     * @exception NoSuccessException
     *            is thrown when the scheme is supported, but the URL
     *            cannot be translated to the scheme.
     */
    public URL translate(Session session, String scheme) throws BadParameterException,
            NoSuccessException;
    
    /**
     * See {@link java.net.URI#resolve(java.net.URI)}.
     * 
     * @param url
     *            the url to resolve with respect to this one.
     * @return the resolved url.
     * @exception NoSuccessException
     *            is thrown when resolving fails for some reason.
     */
    public URL resolve(URL url) throws NoSuccessException;

    /**
     * See {@link java.net.URI#isAbsolute()}.
     * 
     * @return whether this URL is an absolute URL.
     */
    public boolean isAbsolute();

    /**
     * See {@link java.net.URI#normalize()}.
     * 
     * @return a normalized URL.
     */
    public URL normalize();

}
