package org.ogf.saga.isn;

import org.ogf.saga.bootstrap.ImplementationBootstrapLoader;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.url.URL;

/**
 * Factory for objects of the information system navigator package.
 * 
 */
public abstract class ISNFactory {
    
    private static ISNFactory getFactory(String sagaFactoryName)
    		throws NoSuccessException, NotImplementedException {
	return ImplementationBootstrapLoader.getISNFactory(sagaFactoryName);
    }

    /**
     * Creates a <code>EntityDataSet</code> with the default <code>URL</code>.
     * To be provided by the implementation.
     * 
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, the default session
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    protected abstract EntityDataSet doCreateEntityDataSet(String model, String entityName, String filter,
            Session session) throws BadParameterException, DoesNotExistException, NoSuccessException;

    /**
     * Creates a <code>EntityDataSet</code>. To be provided by the
     * implementation.
     * 
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, may be <code>null</code> to denote the
     *            default session
     * @param infoSystemUrl
     *            the URL to guide the implementation, may be <code>null</code>
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    protected abstract EntityDataSet doCreateEntityDataSet(String model, String entityName, String filter,
            Session session, URL infoSystemUrl) throws BadParameterException, DoesNotExistException, NoSuccessException;

    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description.
     * 
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String model, String entityName, String filter)
            throws BadParameterException, DoesNotExistException, NoSuccessException, NotImplementedException {
        return createEntityDataSet(model, entityName, filter, (Session) null);
    }
    

    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String sagaFactoryClassname, String model, String entityName, String filter)
            throws BadParameterException, DoesNotExistException, NoSuccessException, NotImplementedException {
        return createEntityDataSet(sagaFactoryClassname, model, entityName, filter, null);
    }
    

    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description.
     * 
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, may be <code>null</code> to denote the
     *            default session
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String model, String entityName, String filter, Session session)
            throws BadParameterException, DoesNotExistException, NoSuccessException, NotImplementedException {
        return createEntityDataSet(null, model, entityName, filter, session);
    }
    

    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, may be <code>null</code> to denote the
     *            default session
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String sagaFactoryClassname, String model, String entityName, String filter, Session session)
            throws BadParameterException, DoesNotExistException, NoSuccessException, NotImplementedException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateEntityDataSet(model, entityName, filter, session);
    }

    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description. The url
     * specified as an input parameter is to assist the implementation to locate
     * the underlying information system such that it can be queried.
     * 
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, may be <code>null</code> to denote the
     *            default session
     * @param infoSystemUrl
     *            the URL to guide the implementation, may be <code>null</code>
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String model, String entityName, String filter, Session session,
            URL infoSystemUrl) throws BadParameterException, DoesNotExistException, NoSuccessException,
            NotImplementedException {
        return createEntityDataSet(null, model, entityName, filter, session, infoSystemUrl);
    }
    
    /**
     * Creates an <code>EntityDataSet</code> that contains the set of entities
     * that pass the specified filter. The filter MUST only include attributes
     * from the named entity. Details about the filter can be found in the <a
     * href="package-summary.html#filters">package</a> description. The url
     * specified as an input parameter is to assist the implementation to locate
     * the underlying information system such that it can be queried.
     * 
     * @param sagaFactoryClassname
     *      the class name of the Saga factory to be used.
     * @param model
     *            a string containing the name of the information model
     * @param entityName
     *            a string containing the name of the entity to navigate
     * @param filter
     *            a string containing the filter for filtering entities, may be
     *            <code>null</code>
     * @param session
     *            the session handle, may be <code>null</code> to denote the
     *            default session
     * @param infoSystemUrl
     *            the URL to guide the implementation, may be <code>null</code>
     * @return the entityDataSet instance
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws DoesNotExistException
     *             if the url is syntactically valid, but no service can be
     *             contacted at that URL
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @throws NotImplementedException
     *             if not implemented by that SAGA implementation at all
     */
    public static EntityDataSet createEntityDataSet(String sagaFactoryClassname, String model, String entityName, String filter, Session session,
            URL infoSystemUrl) throws BadParameterException, DoesNotExistException, NoSuccessException,
            NotImplementedException {
	if (session == null) {
	    session = SessionFactory.createSession(sagaFactoryClassname);
	}
        return getFactory(sagaFactoryClassname).doCreateEntityDataSet(model, entityName, filter, session, infoSystemUrl);
    }
}
