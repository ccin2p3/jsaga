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
package org.globus.wsrf.impl.security.authentication.encryption;

import java.io.Serializable;

import java.security.cert.X509Certificate;

/**
 * Wrapper class for credentials that will be used for
 * encryption.
 */
public class EncryptionCredentials implements Serializable {

    X509Certificate[] certs;

    public EncryptionCredentials(X509Certificate[] certsArray) {
        this.certs = certsArray;
    }

    public X509Certificate[] getCertificates() {
        return this.certs;
    } 

    public X509Certificate getFirstCertificate() {
        if (this.certs == null) {
            return null;
        } else {
            return this.certs[0];
        }
    }
}
