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
package org.globus.wsrf.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.util.I18n;
import org.globus.wsrf.TopicList;
import org.globus.wsrf.Constants;
import org.globus.wsrf.topicexpression.InvalidTopicExpressionException;
import org.globus.wsrf.topicexpression.TopicExpressionEngine;
import org.globus.wsrf.topicexpression.TopicExpressionResolutionException;
import org.globus.wsrf.topicexpression.TopicExpressionEvaluator;
import org.globus.wsrf.topicexpression.TopicExpressionException;
import org.globus.wsrf.topicexpression.UnsupportedTopicExpressionDialectException;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.Resources;
import org.oasis.wsn.TopicExpressionType;

/**
 * Resolves topic expressions over topic lists.
 * The engine looks for topic expression evaluators under
 * "java:comp/env/topic/eval" context.
 *
 * @see TopicList
 */
public class TopicExpressionEngineImpl implements TopicExpressionEngine
{
    private static String TOPIC_EXPRESSION_EVALUATOR_CONTEXT =
        Constants.JNDI_BASE_NAME + "/topic/eval";

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    static Log logger =
        LogFactory.getLog(TopicExpressionEngineImpl.class.getName());

    private Map evaluators = new HashMap();

    public TopicExpressionEngineImpl()
    {
        this.refresh();
    }

    /**
     * Get the default topic expression engine instance (currently used to hide
     * JNDI lookup details, may change in the future)
     *
     * @return The default topic expression engine instance
     */
    public static TopicExpressionEngine getInstance()
    {
        Context context = null;
        try
        {
            context = new InitialContext();
            return (TopicExpressionEngine) context.lookup(
                Constants.DEFAULT_TOPIC_EXPRESSION_ENGINE);
        }
        catch(NamingException e)
        {
            logger.error(i18n.getMessage("topicEngineConfigError"), e);
        }
        return null;
    }

    /**
     * Refresh the set of registered topic expression evaluators using
     * information discovered from the JNDI registry. This method removes any
     * previously registered topic expression evaluators, so handle with care.
     */
    public synchronized void refresh()
    {
        this.evaluators.clear();
        NamingEnumeration list = null;
        try
        {
            Context context = new InitialContext();
            list = context.list(TOPIC_EXPRESSION_EVALUATOR_CONTEXT);
            NameClassPair pair = null;
            TopicExpressionEvaluator evaluator = null;
            while(list.hasMore())
            {
                pair = (NameClassPair) list.next();
                evaluator = (TopicExpressionEvaluator) JNDIUtils.lookup(
                    context,
                    TOPIC_EXPRESSION_EVALUATOR_CONTEXT + "/" + pair.getName(),
                    TopicExpressionEvaluator.class);
                this.registerEvaluator(evaluator);
            }
        }
        catch(NamingException e)
        {
            logger.error(i18n.getMessage("topicEngineInitError"), e);
        }
        finally
        {
            if(list != null)
            {
                try
                {
                    list.close();
                }
                catch(NamingException ee)
                {
                }
            }
        }
    }

    public synchronized void registerEvaluator(
        TopicExpressionEvaluator evaluator)
    {
        logger.debug("Adding dialects for " + evaluator.getClass().getName());
        String[] dialects = evaluator.getDialects();
        for(int i = 0; i < dialects.length; i++)
        {
            logger.debug("Adding dialect: " + dialects[i]);
            this.evaluators.put(dialects[i], evaluator);
        }
    }

    public synchronized TopicExpressionEvaluator getEvaluator(String dialect)
    {
        TopicExpressionEvaluator evaluator =
            (TopicExpressionEvaluator) this.evaluators.get(dialect);
        return evaluator;
    }

    public synchronized Collection resolveTopicExpression(
        TopicExpressionType topicExpression,
        TopicList topicList)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException
    {
        if(topicExpression == null)
        {
            throw new InvalidTopicExpressionException(
                i18n.getMessage("nullArgument", "topicExpression"));
        }

        if(topicExpression.getDialect() == null)
        {
            throw new UnsupportedTopicExpressionDialectException(
                i18n.getMessage("nullArgument", "topicExpression.dialect"));
        }

        String dialect = topicExpression.getDialect().toString();
        TopicExpressionEvaluator evaluator = this.getEvaluator(dialect);

        if(evaluator == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Dialect not supported:" + dialect);
                logger.debug("Registered dialects are:");
                Iterator keyIterator = this.evaluators.keySet().iterator();
                Object key = null;
                while(keyIterator.hasNext())
                {
                    key = keyIterator.next();
                    logger.debug(key);
                }
                logger.debug(
                    "key.equals(dialect): " + (key.equals(dialect.toString())));
                logger.debug("this.evaluators.containsKey(dialect): " +
                             this.evaluators.containsKey(dialect.toString()));
                logger.debug(
                    "HashCode of key: " + String.valueOf(key.hashCode()));
                logger.debug("HashCode of dialect: " + String.valueOf(
                    dialect.toString().hashCode()));
                logger.debug(
                    "Object stored for key: " + this.evaluators.get(key));
            }
            throw new UnsupportedTopicExpressionDialectException();
        }
        return evaluator.resolve(topicExpression, topicList);
    }

    public synchronized List getConcretePath(
        TopicExpressionType topicExpression)
        throws UnsupportedTopicExpressionDialectException,
               TopicExpressionResolutionException,
               InvalidTopicExpressionException,
               TopicExpressionException
    {
        if(topicExpression == null)
        {
            throw new InvalidTopicExpressionException(
                i18n.getMessage("nullArgument", "topicExpression"));
        }

        if(topicExpression.getDialect() == null)
        {
            throw new UnsupportedTopicExpressionDialectException(
                i18n.getMessage("nullArgument", "topicExpression.dialect"));
        }

        String dialect = topicExpression.getDialect().toString();
        TopicExpressionEvaluator evaluator = this.getEvaluator(dialect);

        if(evaluator == null)
        {
            throw new UnsupportedTopicExpressionDialectException();
        }

        return evaluator.getConcreteTopicPath(topicExpression);
    }

    public synchronized String[] getSupportedDialects()
    {
        return (String[]) this.evaluators.keySet().toArray(
            new String[this.evaluators.size()]);
    }
}
