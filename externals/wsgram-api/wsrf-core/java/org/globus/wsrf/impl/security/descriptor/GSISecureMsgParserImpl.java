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
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.impl.security.descriptor.util.ElementParser;

import javax.xml.namespace.QName;

public class GSISecureMsgParserImpl extends ElementParser
    implements GSIAuthMethodParser {

    private ProtectionLevelParser protLevelParser;

    public GSISecureMsgParserImpl(QName qName) {
        super(qName);
        protLevelParser = new ProtectionLevelParser();
        register(ProtectionLevelParser.QNAME, protLevelParser);
    }

    public AuthMethod getMethod() {
        if (protLevelParser.isPrivacy() && !protLevelParser.isIntegrity()) {
            return GSISecureMsgAuthMethod.PRIVACY;
        } else if (protLevelParser.isIntegrity() && 
                   !protLevelParser.isPrivacy()) {
            return GSISecureMsgAuthMethod.INTEGRITY;
        } else {
            // either both true or both false
            return GSISecureMsgAuthMethod.BOTH;
        }
    }
}
