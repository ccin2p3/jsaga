/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.ws.patched.security.util;

import org.apache.ws.patched.security.WSConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This class is a utility to serialize a DOM node as XML. This class
 * uses the <code>DOM Level 2</code> APIs.
 * The main difference between this class and DOMWriter is that this class
 * generates and prints out namespace declarations.
 *
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 * @author Joseph Kesselman
 */
public class DOM2Writer {
    public static final char NL = '\n';
    public static final String LS = System.getProperty("line.separator",
            (new Character(NL)).toString());

    /**
     * Return a string containing this node serialized as XML.
     */
    public static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        serializeAsXML(node, sw, true);
        return sw.toString();
    }

    /**
     * Return a string containing this node serialized as XML.
     */
    public static String nodeToString(Node node, boolean omitXMLDecl) {
        StringWriter sw = new StringWriter();
        serializeAsXML(node, sw, omitXMLDecl);
        return sw.toString();
    }

    /**
     * Serialize this node into the writer as XML.
     */
    public static void serializeAsXML(Node node, Writer writer,
                                      boolean omitXMLDecl) {
        serializeAsXML(node, writer, omitXMLDecl, false);
    }

    /**
     * Serialize this node into the writer as XML.
     */
    public static void serializeAsXML(Node node, Writer writer,
                                      boolean omitXMLDecl,
                                      boolean pretty) {
        PrintWriter out = new PrintWriter(writer);
        if (!omitXMLDecl) {
            out.print("<?xml version=\"1.0\" encoding=UTF-8 ?>");
        }
        NSStack namespaceStack = new NSStack();
        print(node, namespaceStack, out, pretty, 0);
        out.flush();
    }

    private static void print(Node node, NSStack namespaceStack,
                              PrintWriter out, boolean pretty,
                              int indent) {
        if (node == null) {
            return;
        }
        boolean hasChildren = false;
        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE:
                {
                    NodeList children = node.getChildNodes();
                    if (children != null) {
                        int numChildren = children.getLength();
                        for (int i = 0; i < numChildren; i++) {
                            print(children.item(i), namespaceStack, out,
                                    pretty, indent);
                        }
                    }
                    break;
                }
            case Node.ELEMENT_NODE:
                {
                    namespaceStack.push();
                    if (pretty) {
                        for (int i = 0; i < indent; i++)
                            out.print(' ');
                    }
                    out.print('<' + node.getNodeName());
                    String elPrefix = node.getPrefix();
                    String elNamespaceURI = node.getNamespaceURI();
                    if (elPrefix != null &&
                            elNamespaceURI != null &&
                            elPrefix.length() > 0) {
                        boolean prefixIsDeclared = false;
                        try {
                            String namespaceURI = namespaceStack.getNamespaceURI(elPrefix);
                            if (elNamespaceURI.equals(namespaceURI)) {
                                prefixIsDeclared = true;
                            }
                        } catch (IllegalArgumentException e) {
                        }
                        if (!prefixIsDeclared) {
                            printNamespaceDecl(node, namespaceStack, out);
                        }
                    }
                    NamedNodeMap attrs = node.getAttributes();
                    int len = (attrs != null) ? attrs.getLength() : 0;
                    for (int i = 0; i < len; i++) {
                        Attr attr = (Attr) attrs.item(i);
                        out.print(' ' + attr.getNodeName() + "=\"");
                        normalize(attr.getValue(), out);
                        out.print('\"');
                        String attrPrefix = attr.getPrefix();
                        String attrNamespaceURI = attr.getNamespaceURI();
                        if (attrPrefix != null && attrNamespaceURI != null) {
                            boolean prefixIsDeclared = false;
                            try {
                                String namespaceURI = namespaceStack.getNamespaceURI(attrPrefix);
                                if (attrNamespaceURI.equals(namespaceURI)) {
                                    prefixIsDeclared = true;
                                }
                            } catch (IllegalArgumentException e) {
                            }
                            if (!prefixIsDeclared) {
                                printNamespaceDecl(attr, namespaceStack, out);
                            }
                        }
                    }
                    NodeList children = node.getChildNodes();
                    if (children != null) {
                        int numChildren = children.getLength();
                        hasChildren = (numChildren > 0);
                        if (hasChildren) {
                            out.print('>');
                            if (pretty)
                                out.print(LS);
                        }
                        for (int i = 0; i < numChildren; i++) {
                            print(children.item(i), namespaceStack, out, pretty,
                                    indent + 1);
                        }
                    } else {
                        hasChildren = false;
                    }
                    if (!hasChildren) {
                        out.print("/>");
                        if (pretty)
                            out.print(LS);
                    }
                    namespaceStack.pop();
                    break;
                }
            case Node.ENTITY_REFERENCE_NODE:
                {
                    out.print('&');
                    out.print(node.getNodeName());
                    out.print(';');
                    break;
                }
            case Node.CDATA_SECTION_NODE:
                {
                    out.print("<![CDATA[");
                    out.print(node.getNodeValue());
                    out.print("]]>");
                    break;
                }
            case Node.TEXT_NODE:
                {
                    normalize(node.getNodeValue(), out);
                    break;
                }
            case Node.COMMENT_NODE:
                {
                    out.print("<!--");
                    out.print(node.getNodeValue());
                    out.print("-->");
                    if (pretty)
                        out.print(LS);
                    break;
                }
            case Node.PROCESSING_INSTRUCTION_NODE:
                {
                    out.print("<?");
                    out.print(node.getNodeName());
                    String data = node.getNodeValue();
                    if (data != null && data.length() > 0) {
                        out.print(' ');
                        out.print(data);
                    }
                    out.println("?>");
                    if (pretty)
                        out.print(LS);
                    break;
                }
        }
        if (type == Node.ELEMENT_NODE && hasChildren == true) {
            if (pretty) {
                for (int i = 0; i < indent; i++)
                    out.print(' ');
            }
            out.print("</");
            out.print(node.getNodeName());
            out.print('>');
            if (pretty)
                out.print(LS);
            hasChildren = false;
        }
    }

    private static void printNamespaceDecl(Node node,
                                           NSStack namespaceStack,
                                           PrintWriter out) {
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                {
                    printNamespaceDecl(((Attr) node).getOwnerElement(), node,
                            namespaceStack, out);
                    break;
                }
            case Node.ELEMENT_NODE:
                {
                    printNamespaceDecl((Element) node, node, namespaceStack, out);
                    break;
                }
        }
    }

    private static void printNamespaceDecl(Element owner, Node node,
                                           NSStack namespaceStack,
                                           PrintWriter out) {
        String namespaceURI = node.getNamespaceURI();
        String prefix = node.getPrefix();
        if (!(namespaceURI.equals(WSConstants.XMLNS_NS) && prefix.equals("xmlns")) &&
                !(namespaceURI.equals(WSConstants.XML_NS) && prefix.equals("xml"))) {
            if (WSSecurityUtil.getNamespace(prefix, owner) == null) {
                out.print(" xmlns:" + prefix + "=\"" + namespaceURI + '\"');
            }
        } else {
            prefix = node.getLocalName();
            namespaceURI = node.getNodeValue();
        }
        namespaceStack.add(namespaceURI, prefix);
    }

    /**
     * Normalizes and prints the given string.
     */
    public static void normalize(String s, PrintWriter fOut) {
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    {
                        fOut.print("&lt;");
                        break;
                    }
                case '>':
                    {
                        fOut.print("&gt;");
                        break;
                    }
                case '&':
                    {
                        fOut.print("&amp;");
                        break;
                    }
                case '"':
                    {
                        fOut.print("&quot;");
                        break;
                    }
                case '\r':
                case '\n':
                default:
                    {
                        fOut.print(c);
                    }
            }
        }
    }
}
