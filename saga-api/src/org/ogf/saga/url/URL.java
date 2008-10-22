package org.ogf.saga.url;

import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.NoSuccess;

public interface URL {

    /**
     * Replaces the current value of the URL with the specified value.
     * 
     * @param url
     *            the string.
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setString(String url) throws BadParameter;

    /**
     * Returns this URL as a string.
     * 
     * @return the string.
     */
    public String getString();

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setFragment(String fragment) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setHost(String host) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the path.
     */
    public void setPath(String path) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     *                (???)
     */
    public void setPort(int port) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setQuery(String query) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setScheme(String scheme) throws BadParameter;

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
     * @exception BadParameter
     *                is thrown when there is a syntax error in the parameter.
     */
    public void setUserInfo(String userInfo) throws BadParameter;

    /**
     * Returns a new URL with the scheme part replaced.
     * 
     * @param scheme
     *            the new scheme.
     * @return the new URL.
     * @exception BadParameter
     *                is thrown when there is a syntax error in the new URL.
     */
    public URL translate(String scheme) throws BadParameter, NoSuccess;

    /**
     * See {@link java.net.URI#resolve(java.net.URI)}.
     * 
     * @param url
     *            the url to resolve with respect to this one.
     * @return the resolved url.
     */
    public URL resolve(URL url) throws NoSuccess;

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