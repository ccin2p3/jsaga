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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.Text;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

import org.globus.wsrf.WSRFConstants;
import org.globus.wsrf.query.ExpressionEvaluator;
import org.globus.wsrf.query.QueryException;
import org.globus.wsrf.query.UnsupportedQueryDialectException;
import org.globus.wsrf.query.QueryEvaluationException;
import org.globus.wsrf.query.InvalidQueryExpressionException;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.utils.XmlUtils;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

import org.oasis.wsrf.properties.QueryExpressionType;

/**
 * Evaluator supporting XPath queries using Xalan-J XPathAPI.
 * The implementation creates resource property document on-fly and runs
 * the query against it.
 */
public class XPathExpressionEvaluator implements ExpressionEvaluator {

    static Log logger =
        LogFactory.getLog(XPathExpressionEvaluator.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    private static final String [] DIALECTS =
        new String [] { WSRFConstants.XPATH_1_DIALECT };

    public String[] getDialects() {
        return DIALECTS;
    }

    public Object evaluate(QueryExpressionType expression,
                           ResourcePropertySet resourcePropertySet)
        throws UnsupportedQueryDialectException,
               QueryEvaluationException,
               InvalidQueryExpressionException,
               QueryException {
        try {
            return evaluateQuery(expression, resourcePropertySet);
        } catch (QueryException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryEvaluationException(e);
        }
    }

    private List evaluateQuery(QueryExpressionType expression,
                               ResourcePropertySet resourcePropertySet)
        throws Exception {
        if (expression == null) {
            throw new QueryException(i18n.getMessage("noQuery"));
        }
        if (expression.getDialect() == null) {
            throw new QueryException(
                i18n.getMessage("nullArgument", "expression.dialect")
            );
        }
        String dialect = expression.getDialect().toString();
        if (!(dialect.equals(WSRFConstants.XPATH_1_DIALECT))) {
            throw new UnsupportedQueryDialectException(
                i18n.getMessage("invalidQueryExpressionDialect"));
        }

        if (expression.getValue() == null ||
            expression.getValue().toString().trim().length() == 0) {
            throw new InvalidQueryExpressionException(
                i18n.getMessage("noQueryString"));
        }

        // TODO: error checking?
        String query = expression.getValue().toString().trim();

        logger.debug("Query: " + query);

        // Turn ResourcePropertySet into one big DOM element
        Element rootElement = resourcePropertySet.toElement();

        // collects all NS in the document and puts them in the root element
        // that root element is then used for xpath query to resolve all
        // namespaces defined in the xpath query.

        // XXX: The namespaces should probably be somehow passed with the query
        // this will require spec changes
        Map namespaces = new HashMap();
        collectNamespaces(rootElement, namespaces, new HashMap());
        setNamespaces(rootElement, namespaces);

        if (logger.isDebugEnabled()) {
            logger.debug("Document: " + XmlUtils.toString(rootElement));
        }

        XObject result = XPathAPI.eval(rootElement,
                                       query,
                                       rootElement);

        ArrayList resultList = null;

        if (result instanceof XBoolean ||
            result instanceof XNumber ||
            result instanceof XString) {
            resultList = new ArrayList(1);
            MessageElement element = 
                new MessageElement(new Text( result.str() ));
            resultList.add(element);
        } else if (result instanceof XNodeSet) {
            XNodeSet set = (XNodeSet)result;
            NodeList list = set.nodelist();
            resultList = new ArrayList(list.getLength());
            for (int i=0;i<list.getLength();i++) {
                Node node = list.item(i);
                if (node instanceof Document) {
                    resultList.add(new MessageElement(((Document)node).getDocumentElement()));
                } else if (node instanceof Element) {
                    resultList.add(new MessageElement((Element)node));
                } else if (node instanceof CharacterData) {
                    resultList.add(new MessageElement(new Text(((CharacterData)node).getData())));
                } else {
                    throw new QueryException(i18n.getMessage(
                        "unsupportedXpathReturn", node.getClass().getName()));
                }
            }
        } else {
            throw new QueryException(i18n.getMessage(
                "unsupportedXpathReturn", result));
        }

        return resultList;
    }

    private void collectNamespaces(Node node, Map namespaces, Map prefixes) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i=0;i<attributes.getLength();i++) {
                Attr attr = (Attr)attributes.item(i);
                String name = attr.getName();
                String value = attr.getValue();
                if (name.startsWith("xmlns:")) {
                    if (namespaces.get(value) == null) {
                        // ns not defined
                        if (prefixes.get(name) != null) {
                            // find unique prefix
                            int j = 1;
                            do {
                                name = "xmlns:ns" + j++;
                            } while ( prefixes.get(name) != null);
                        }
                        prefixes.put(name, value);
                        namespaces.put(value, name);
                    }
                }
            }
        }
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i=0;i<children.getLength();i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    collectNamespaces(child, namespaces, prefixes);
                }
            }
        }
    }

    private void setNamespaces(Element node, Map namespaces) {
        Iterator iter = namespaces.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            node.setAttributeNS("http://www.w3.org/2000/xmlns/",
                                (String)entry.getValue(),
                                (String)entry.getKey());
        }
    }

}

