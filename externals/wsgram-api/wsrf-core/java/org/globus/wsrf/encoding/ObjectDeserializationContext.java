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
package org.globus.wsrf.encoding;

import java.io.StringReader;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Constants;
import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.message.EnvelopeHandler;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHandler;
import org.apache.axis.utils.XMLUtils;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;

import org.globus.util.I18n;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.config.ContainerConfig;

public class ObjectDeserializationContext extends DeserializationContext {

    static Log logger =
        LogFactory.getLog(ObjectDeserializationContext.class.getName());

    static I18n i18n = I18n.getI18n(Resources.class.getName());

    private Deserializer topDeserializer = null;

    public ObjectDeserializationContext(MessageElement element)
        throws DeserializationException {
        this(element, null);
    }

    public ObjectDeserializationContext(MessageElement element,
                                        Class javaClass)
        throws DeserializationException {
        super(ContainerConfig.getContext(), new SOAPHandler());

        init(element.getType(), javaClass);

        String inputString = element.toString();
        logger.debug(inputString);
        inputSource = new InputSource(new StringReader(inputString));
    }

    public ObjectDeserializationContext(Element element)
        throws DeserializationException {
        this(element, null);
    }

    public ObjectDeserializationContext(Element element, Class javaClass)
        throws DeserializationException {
        super(ContainerConfig.getContext(), new SOAPHandler());

        String typeAttr =
            element.getAttributeNS(Constants.URI_DEFAULT_SCHEMA_XSI, "type");

        QName type = null;

        if (typeAttr != null && typeAttr.length() > 0) {
            type = XMLUtils.getQNameFromString(typeAttr,
                                               element);
        }

        init(type, javaClass);

        String inputString = XMLUtils.ElementToString(element);
        logger.debug(inputString);
        inputSource = new InputSource(new StringReader(inputString));
    }

    public ObjectDeserializationContext(InputSource input, Class javaClass)
        throws DeserializationException {
        super(ContainerConfig.getContext(), new SOAPHandler());
        init(null, javaClass);
        this.inputSource = input;
    }

    private void setDeserializer(QName type, Class javaClass) 
        throws DeserializationException {
        if (type == null && javaClass == null) {
            throw new DeserializationException(
                        i18n.getMessage("typeOrClassRequired"));
        }

        if (type != null) {
            // Use the xmlType to get the deserializer.
            this.topDeserializer = getDeserializerForType(type);
        } else {
            QName defaultXMLType = getTypeMapping().getTypeQName(javaClass);
            this.topDeserializer = getDeserializer(javaClass, defaultXMLType);
        }
        
        if (this.topDeserializer == null) {
            this.topDeserializer = getDeserializerForClass(javaClass);
        }
    }

    private void init(QName type, Class javaClass)
        throws DeserializationException {
        msgContext.setEncodingStyle("");
        popElementHandler();

        setDeserializer(type, javaClass);

        if (topDeserializer == null) {
            String arg = (type == null) ? 
                javaClass.getName() : type.toString();
            throw new DeserializationException(
                i18n.getMessage("noDeserializer", arg));
        }

        pushElementHandler(
              new EnvelopeHandler((SOAPHandler) this.topDeserializer)
        );
    }

    // overwrites the superclass method
    public void setDocumentLocator(Locator locator) {}

    public Object getValue() {
        return (this.topDeserializer == null) ?
            null :
            this.topDeserializer.getValue();
    }

    public MessageElement getMessageElement() {
        if (this.topDeserializer == null ||
            !(this.topDeserializer instanceof SOAPHandler)) {
            return null;
        }
        return ((SOAPHandler)this.topDeserializer).myElement;
    }

    public QName getQName() {
        MessageElement element = getMessageElement();
        return (element == null) ? null : element.getQName();
    }

}
