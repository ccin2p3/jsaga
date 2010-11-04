package org.ogf.saga.isn;

import java.util.List;
import java.util.Set;

import org.ogf.saga.SagaObject;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;

/**
 * <p>
 * Provides the means to navigate around the information model for a selected
 * entity and gives access to the {@link EntityData} objects.
 * </p>
 * <p>
 * Navigation consists of moving from entity to entity within an information
 * model, as expressed in the GLUE entity relationship model. Navigation can
 * also be from entity to related entity. A list of possible navigation steps
 * from an <code>EntityDataSet</code> object is returned by the
 * <code>listRelatedEntityNames</code> method. Navigation to a set of related
 * entities is achieved with the <code>getRelatedEntities</code> method, which
 * returns a new <code>EntityDataSet</code> object. N.B. navigation is from a
 * set of <code>EntityData</code> objects to a new set, a many to many
 * relationship.
 * </p>
 * In order to restrict the number of <code>EntityData</code> objects returned
 * in the <code>EntityDataSet</code> object, a filter may be used with the
 * <code>getRelatedEntities</code> method. The filter MUST only include
 * attributes from the related entity and it will be applied to the related
 * entities.
 * </p>
 */
public interface EntityDataSet extends SagaObject {

    /**
     * Returns a set of <code>EntityData</code> objects.
     * 
     * @return a set of <code>EntityData</code> objects associated with this
     *         entity
     */
    public Set<EntityData> getData();

    /**
     * Returns an <code>EntityDataSet</code> object for the given entity name
     * and matching the filter string. The filter MUST only include attributes
     * from the related entity. More details about the filter can be found in
     * the <a href="package-summary.html#filters">package</a> description. N.B.
     * There is a special case where there is a self relationship between
     * entities, i.e. "AdminDomain" in GLUE 2, in such cases the keywords
     * <code>up</code> and <code>down</code> may be used in place of the
     * name of the related entity to navigate to. For example where
     * AdminDomain="rl.ac.uk" up may return AdminDomain="ac.uk".
     * 
     * @param relatedName
     *            a string containing the name of the related entity to navigate
     *            to
     * @param filter
     *            a string containing the filter for filtering related entities,
     *            may be <code>null</code>
     * @return a related entity data set matching the specified filter string
     * @throws BadParameterException
     *             if the related name is not valid, or the filter is not valid
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @see #listRelatedEntityNames()
     */
    public EntityDataSet getRelatedEntities(String relatedName, String filter) throws BadParameterException,
            NoSuccessException;

    /**
     * Returns an <code>EntityDataSet</code> object for the given entity name.
     * N.B. There is a special case where there is a self relationship between
     * entities, i.e. "AdminDomain" in GLUE 2, in such cases the keywords
     * <code>up</code> and <code>down</code> may be used in place of the
     * name of the related entity to navigate to. For example where
     * AdminDomain="rl.ac.uk" up may return AdminDomain="ac.uk".
     * 
     * @param relatedName
     *            a string containing the name of the related entity to navigate
     *            to
     * @return a related entity data set
     * @throws BadParameterException
     *             if the related name is not valid
     * @throws NoSuccessException
     *             if no result can be returned because of information system or
     *             other internal problems
     * @see #listRelatedEntityNames()
     */
    public EntityDataSet getRelatedEntities(String relatedName) throws BadParameterException, NoSuccessException;

    /**
     * Returns a set of names of those entities that may be navigated to, from
     * this EntityDataSet. N.B There is a special case where there is a self
     * relationship between entities, i.e. "AdminDomain" in GLUE 2, in such
     * cases the keywords <code>up</code> and <code>down</code> will also be
     * returned as appropriate.
     * 
     * @return a list of names of related entities
     */
    public List<String> listRelatedEntityNames();

}
