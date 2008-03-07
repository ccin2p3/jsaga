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
package org.globus.wsrf.impl.properties;

import java.rmi.RemoteException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.query.QueryEngine;
import org.globus.wsrf.query.UnsupportedQueryDialectException;
import org.globus.wsrf.query.QueryException;
import org.globus.wsrf.query.QueryEvaluationException;
import org.globus.wsrf.query.InvalidQueryExpressionException;
import org.globus.wsrf.utils.AnyHelper;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.FaultHelper;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.oasis.wsrf.properties.InvalidQueryExpressionFaultType;
import org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType;
import org.oasis.wsrf.properties.QueryEvaluationErrorFaultType;
import org.oasis.wsrf.properties.QueryExpressionType;
import org.oasis.wsrf.properties.ResourceUnknownFaultType;
import org.oasis.wsrf.properties.UnknownQueryExpressionDialectFaultType;
import org.oasis.wsrf.properties.QueryResourceProperties_Element;
import org.oasis.wsrf.properties.QueryResourcePropertiesResponse;

import javax.xml.soap.SOAPElement;

/**
 * QueryResourceProperties operation implementation. It looks for a QueryEngine
 * implementation under "java:comp/env/query/ContainerQueryEngine".
 */
public class QueryResourcePropertiesProvider {

    static Log logger =
        LogFactory.getLog(QueryResourcePropertiesProvider.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public QueryResourcePropertiesResponse queryResourceProperties(QueryResourceProperties_Element request)
        throws RemoteException,
               InvalidResourcePropertyQNameFaultType,
               ResourceUnknownFaultType,
               InvalidQueryExpressionFaultType,
               QueryEvaluationErrorFaultType,
               UnknownQueryExpressionDialectFaultType {

        if (request == null) {
            throw new RemoteException(
                i18n.getMessage("nullArgument", "request"));
        }

        Object resource = null;
        try {
            resource = ResourceContext.getResourceContext().getResource();
        } catch (NoSuchResourceException e) {
            ResourceUnknownFaultType fault = 
                new ResourceUnknownFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (Exception e) {
            throw new RemoteException(
                i18n.getMessage("resourceDisoveryFailed"), e);
        }

        if (!(resource instanceof ResourceProperties)) {
            throw new RemoteException(i18n.getMessage("rpsNotSupported"));
        }

        ResourcePropertySet set =
            ((ResourceProperties)resource).getResourcePropertySet();

        QueryEngine engine = null;
        try {
            Context initialContext = new InitialContext();
            engine = (QueryEngine)JNDIUtils.lookup(
                                 initialContext,
                                 Constants.DEFAULT_QUERY_ENGINE,
                                 QueryEngine.class
                     );
        } catch (NamingException e) {
            throw new RemoteException(
                i18n.getMessage("queryEngineLookupError"), e
            );
        }

        QueryExpressionType query = request.getQueryExpression();

        Object result = null;
        try {
            result = engine.executeQuery(query, set);
        } catch (UnsupportedQueryDialectException e) {
            UnknownQueryExpressionDialectFaultType fault =
                new UnknownQueryExpressionDialectFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (QueryEvaluationException e) {
            QueryEvaluationErrorFaultType fault =
                new QueryEvaluationErrorFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (InvalidQueryExpressionException e) {
            InvalidQueryExpressionFaultType fault =
                new InvalidQueryExpressionFaultType();
            FaultHelper faultHelper = new FaultHelper(fault);
            faultHelper.addFaultCause(e);
            throw fault;
        } catch (QueryException e) {
            throw new RemoteException(i18n.getMessage("queryFailed"), e);
        }

        QueryResourcePropertiesResponse response =
            new QueryResourcePropertiesResponse();
        
        if (result instanceof List) {
            AnyHelper.setAny(response, (List)result);
        } else if (result instanceof SOAPElement) {
            AnyHelper.setAny(response, (SOAPElement)result);
        } else if (result instanceof SOAPElement[]) {
            AnyHelper.setAny(response, (SOAPElement[])result);
        } else {
            throw new RemoteException(
              i18n.getMessage("unsupportedQueryReturnType",
                              new Object [] {result.getClass().getName() }));
        }
        
        return response;
    }
}
