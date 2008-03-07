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
 * The <code>TopicExpressionEngine</code> interface is used to map queries on a
 * topic list to the appropriate <code>TopicExpressionEvaluators</code> and then
 * return the result. <code>TopicExpressionEvaluators</code> can be
 * preconfigured or dynamically added at runtime.
 *
 * @see TopicExpressionEvaluator
 */
public interface TopicExpressionEngine
{
    /**
     * registers a new evaluator that can be used to evaluate topic expressions
     *
     * @param evaluator implementation of evaluator to be used for evaluating
     *                  topic expressions
     */
    public void registerEvaluator(TopicExpressionEvaluator evaluator);

    /**
     * Resolves a topic expression using the passed topic list. The appropraite
     * TopicExpressionEvaluator is used based on the dialect element.
     *
     * @param topicExpression topic expression
     * @param topicList       the topic list to apply the expression to
     * @return the set of topics the expression evaluated to
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
    public Collection resolveTopicExpression(
        TopicExpressionType topicExpression,
        TopicList topicList) throws UnsupportedTopicExpressionDialectException,
                                    TopicExpressionResolutionException,
                                    InvalidTopicExpressionException,
                                    TopicExpressionException;

    /**
     * Converts the expression from dialect specific form to a ordered list of
     * QNames. This method throws an exception if the expression does not
     * evaluate to a concrete topic path.
     *
     * @param topicExpression topic expression
     * @return a list of QNames describing the concrete topic path
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
    public List getConcretePath(TopicExpressionType topicExpression)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException;

    /**
     * Gets the evaluator currently registered to handle a topic expression of
     * the specified dialect.
     *
     * @return the matching topic expression evaluator or null if none was
     *         found
     */
    public TopicExpressionEvaluator getEvaluator(String dialect);

    /**
     * Returns a list of URIs representing the registered topic expression
     * dialects
     *
     * @return the list of supported dialects
     */
    public String[] getSupportedDialects();
}
