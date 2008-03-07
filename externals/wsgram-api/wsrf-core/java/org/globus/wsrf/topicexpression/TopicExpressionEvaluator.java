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
package org.globus.wsrf.topicexpression;

import java.util.Collection;
import java.util.List;

import org.globus.wsrf.TopicList;
import org.oasis.wsn.TopicExpressionType;

/**
 * A <code>TopicExpressionEvaluator</code> is used to implement a topic
 * expression evaluation against a <code>TopicList</code>. An evaluator can be
 * registered with a <code>TopicExpressionEngine</code>, which in turn calls the
 * evaluator when a matching expression is found.
 *
 * @see org.globus.wsrf.TopicList
 * @see org.globus.wsrf.topicexpression.TopicExpressionEngine
 */
public interface TopicExpressionEvaluator
{

    /**
     * Evaluates the expression over a TopicList and returns the result.
     *
     * @param expression object passed by client representing the topic
     *                   expression
     * @param topicList  topic list associated with the service/resource
     * @return the result of the evaluation which depends on the expression.
     * @throws UnsupportedTopicExpressionDialectException
     *                                  if the topic expression dialect is not
     *                                  supported
     * @throws TopicExpressionResolutionException
     *                                  if the expression could not be
     *                                  evaluated
     * @throws InvalidTopicExpressionException
     *                                  if the topic expression is invalid
     * @throws TopicExpressionException if any other error occurs
     */
    public Collection resolve(
        TopicExpressionType expression,
        TopicList topicList)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException;

    /**
     * Gets the URIs for the dialects that this evaluator can handle
     *
     * @return array of URIs supported by this evaluator
     */
    public String[] getDialects();

    /**
     * Converts the expression from dialect specific form to a ordered list of
     * QNames. This method throws an exception if the expression does not
     * evaluate to a concrete topic path.
     *
     * @param expression object passed by client representing the topic
     *                   expression
     * @return a list of QNames describing the concrete topic path
     * @throws UnsupportedTopicExpressionDialectException
     *                                  if the topic expression dialect is not
     *                                  supported
     * @throws InvalidTopicExpressionException
     *                                  if the topic expression is invalid
     * @throws TopicExpressionException if any other error occurs
     */
    public List getConcreteTopicPath(TopicExpressionType expression)
        throws UnsupportedTopicExpressionDialectException,
               InvalidTopicExpressionException,
               TopicExpressionException;

    /**
     * Converts a topic path (list of QNames) to a dialect specific concrete
     * topic expression.
     *
     * @param topicPath containing a list of QNames describing a concrete topic
     *                  path
     * @return dialect specific version of the topic path
     * @throws InvalidTopicExpressionException
     *                                  if the conrete topic path is invalid
     * @throws TopicExpressionException if any other error occurs
     */
    public TopicExpressionType toTopicExpression(List topicPath)
        throws InvalidTopicExpressionException,
               TopicExpressionException;
}
