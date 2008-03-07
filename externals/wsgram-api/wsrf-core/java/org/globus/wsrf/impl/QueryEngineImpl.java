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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.query.ExpressionEvaluator;
import org.globus.wsrf.query.QueryEngine;
import org.globus.wsrf.query.QueryException;
import org.globus.wsrf.query.UnsupportedQueryDialectException;
import org.globus.wsrf.query.QueryEvaluationException;
import org.globus.wsrf.query.InvalidQueryExpressionException;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;

import org.oasis.wsrf.properties.QueryExpressionType;

/**
 * Executes queries on resource property sets.
 * The engine looks for evaluators under "java:comp/env/query/eval" context.
 * @see ResourcePropertySet
 */
public class QueryEngineImpl implements QueryEngine {

    static Log logger =
        LogFactory.getLog(QueryEngineImpl.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static String QUERY_EVALUATOR_CONTEXT =
        Constants.JNDI_BASE_NAME + "/query/eval";

    private Hashtable evaluators = new Hashtable();

    public QueryEngineImpl() {
        refresh();
    }

    /**
     * Reinitializes the evaluators list from JNDI context. If any evaluators
     * were added using {@link #registerEvaluator(ExpressionEvaluator)
     * registerEvaluator()} function they will be lost.
     */
    public synchronized void refresh() {
        evaluators.clear();
        NamingEnumeration list = null;
        try {
            Context initialContext = new InitialContext();
            list = initialContext.list(QUERY_EVALUATOR_CONTEXT);
            NameClassPair pair = null;
            ExpressionEvaluator evaluator = null;
            while(list.hasMore()) {
                pair = (NameClassPair)list.next();
                evaluator = (ExpressionEvaluator)JNDIUtils.lookup(
                               initialContext,
                               QUERY_EVALUATOR_CONTEXT + "/" + pair.getName(),
                               ExpressionEvaluator.class
                             );
                registerEvaluator(evaluator);
            }
        } catch (NamingException e) {
            logger.error(i18n.getMessage("queryEngineInitError"), e);
        } finally {
            if (list != null) {
                try { list.close(); } catch (NamingException ee) {}
            }
        }
    }

    public void registerEvaluator(ExpressionEvaluator evaluator) {
        String[] names = evaluator.getDialects();
        for (int i = 0; i < names.length; i++) {
            this.evaluators.put(names[i], evaluator);
        }
    }

    public ExpressionEvaluator getEvaluator(String dialect) {
        ExpressionEvaluator evaluator =
            (ExpressionEvaluator) this.evaluators.get(dialect);
        return evaluator;
    }

    public Object executeQuery(QueryExpressionType expression,
                               ResourcePropertySet resourcePropertySet)
        throws UnsupportedQueryDialectException,
               QueryEvaluationException,
               InvalidQueryExpressionException,
               QueryException {
        if (expression == null) {
            throw new QueryException(i18n.getMessage("noQuery"));
        }
        if (expression.getDialect() == null) {
            throw new QueryException(
                i18n.getMessage("nullArgument", "expression.dialect")
            );
        }
        String dialect = expression.getDialect().toString();
        ExpressionEvaluator evaluator = getEvaluator(dialect);
        if (evaluator == null) {
            throw new UnsupportedQueryDialectException(
                i18n.getMessage("unsupportedQueryDialect", dialect)
            );
        }
        return evaluator.evaluate(expression, resourcePropertySet);
    }
}
