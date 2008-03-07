/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.query;

import org.globus.wsrf.ResourcePropertySet;
import org.oasis.wsrf.properties.QueryExpressionType;

/**
 * The <code>QueryEngine</code> interface is used to map queries on the service
 * data of a service to the appropriate <code>ExpressionEvaluators</code> and
 * then return the result. <code>ExpressionEvaluators</code> can be
 * preconfigured or dynamically added at runtime.
 * @see ExpressionEvaluator
 */
public interface QueryEngine {

    /**
     * registers a new evaluator that can be used to evaluate queries on a
     * service.
     * @param evaluator implementation of evaluator to be used for evaluating
     * queries
     * specified in its XML Schema definition.
     */
    public void registerEvaluator(ExpressionEvaluator evaluator);

    /**
     * Executes a query against a resource property set. The appropraite
     * ExpressionEvaluator is used for the query based on the dialect
     * attribute.
     *
     * @param queryExpression query expression
     * @param resourcePropertySet resource properties set to execute the query
     *        against
     * @return the result of the evaluation which depends on the expression.
     *         The results must be an xml serializable object in order to be
     *         passed back correctly to a remote client.
     *         The easiest way of achieving this is to model it as a Bean, or
     *         simply return a <code>SOAPElment</code> or
     *         <code>DOM Element</code>.
     *         If the result object returned is null an empty query result
     *         is returned.
     * @throws UnsupportedQueryDialectException if query dialect is
     *         unsupported.
     * @throws QueryEvaluationException if query evaluation fails.
     * @throws InvalidQueryExpressionException if query expression is invalid.
     * @throws QueryException if any other error
     */
    public Object executeQuery(QueryExpressionType queryExpression,
                               ResourcePropertySet resourcePropertySet)
        throws UnsupportedQueryDialectException,
               QueryEvaluationException,
               InvalidQueryExpressionException,
               QueryException;

    /**
     * Gets the evaluator currently registered to handle an expression of the
     * specified qualified name (from the top level element of the XML Schema
     * definition of the expression)
     * @return the matching expression evaluator or null if none was found
     */
    public ExpressionEvaluator getEvaluator(String dialect);

}
