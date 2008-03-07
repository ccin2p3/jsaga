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
 * An <code>ExpressionEvaluator</code> is used to implement a query expression
 * evaluation of a <code>ResourcePropertySet</code>. An evaluator can be
 * registered with a <code>QueryEngine</code>, which in turn calls the 
 * evaluator when a matching expression is found.
 *
 * @see ResourcePropertySet
 * @see QueryEngine
 */
public interface ExpressionEvaluator {

    /**
     * Evaluates the expression over a ResourcePropertySet and returns the 
     * result.
     *
     * @param expression object passed by client representing query expression.
     * @param resourcePropertySet ResourcePropertySet associated with resource.
     *        The expression is evaluated against this set.
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
    public Object evaluate(QueryExpressionType expression,
                           ResourcePropertySet resourcePropertySet)
        throws UnsupportedQueryDialectException,
               QueryEvaluationException,
               InvalidQueryExpressionException,
               QueryException;
    
    /**
     * Gets the list of dialects that this evaluator can handle.
     *
     * @return list of dialects supported by this evaluator.
     */
    public String[] getDialects();
    
}
