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
package org.globus.wsrf.impl.security.authentication.signature;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.algorithms.SignatureAlgorithmSpi;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Base64;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.providers.GSSPrivateKey;
import org.globus.wsrf.providers.GSSPublicKey;

/**
 * This is a dummy class so that the xml-security library recognizes
 * "http://www.globus.org/2002/04/xmlenc#gssapi-sign" as signature algorithm.
 * 
 */
public class SignatureGSS extends SignatureAlgorithmSpi
{
    private static Log logger = LogFactory.getLog(SignatureGSS.class.getName());

    /**
     * Field URI
     */
    public static final String URI =
        "http://www.globus.org/2002/04/xmlenc#gssapi-sign";

    /**
     * Field algorithm
     */
    private Signature signatureAlgorithm = null;

    protected String engineGetURI()
    {
        return SignatureGSS.URI;
    }

    public SignatureGSS() throws XMLSignatureException
    {
        String algorithmID = JCEMapper.translateURItoJCEID(SignatureGSS.URI);

        logger.debug("Created SignatureGSS using " + algorithmID);

        try
        {
            this.signatureAlgorithm = Signature.getInstance(algorithmID);
        }
        catch(java.security.NoSuchAlgorithmException ex)
        {
            Object[] exArgs = {algorithmID,
                               ex.getLocalizedMessage()};

            throw new XMLSignatureException("algorithms.NoSuchAlgorithm",
                                            exArgs);
        }
    }

    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws XMLSignatureException
    {
        try
        {
            this.signatureAlgorithm.setParameter(params);
        }
        catch(InvalidAlgorithmParameterException e)
        {
            throw new XMLSignatureException("empty", e);
        }
    }

    protected boolean engineVerify(byte[] signature)
        throws XMLSignatureException
    {
        try
        {
            logger.debug("Called GSS.verify() on " + Base64.encode(signature));

            return this.signatureAlgorithm.verify(signature);
        }
        catch(SignatureException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(
        Key gssKey,
        SecureRandom secureRandom)
        throws XMLSignatureException
    {
        if(!(gssKey instanceof GSSPrivateKey))
        {
            String supplied = gssKey.getClass().getName();
            String needed = GSSPrivateKey.class.getName();
            Object exArgs[] = {supplied, needed};

            throw new XMLSignatureException(
                "algorithms.WrongKeyForThisOperation",
                exArgs);
        }

        try
        {
            this.signatureAlgorithm.initSign((PrivateKey) gssKey,
                                             secureRandom);
        }
        catch(InvalidKeyException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected byte[] engineSign() throws XMLSignatureException
    {
        try
        {
            return this.signatureAlgorithm.sign();
        }
        catch(SignatureException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte[] input) throws XMLSignatureException
    {
        try
        {
            this.signatureAlgorithm.update(input);
        }
        catch(SignatureException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(byte input) throws XMLSignatureException
    {
        try
        {
            this.signatureAlgorithm.update(input);
        }
        catch(SignatureException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineUpdate(
        byte[] buf,
        int offset,
        int len) throws XMLSignatureException
    {
        try
        {
            this.signatureAlgorithm.update(buf, offset, len);
        }
        catch(SignatureException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected String engineGetJCEAlgorithmString()
    {
        return this.signatureAlgorithm.getAlgorithm();
    }

    protected String engineGetJCEProviderName()
    {
        return this.signatureAlgorithm.getProvider().getName();
    }

    protected void engineSetHMACOutputLength(int HMACOutputLength)
        throws XMLSignatureException
    {
        throw new XMLSignatureException(
            "algorithms.HMACOutputLengthOnlyForHMAC");
    }

    protected void engineInitVerify(Key gssKey) throws XMLSignatureException
    {
        if(!(gssKey instanceof GSSPublicKey))
        {
            String supplied = gssKey.getClass().getName();
            String needed = GSSPublicKey.class.getName();
            Object exArgs[] = {supplied, needed};

            throw new XMLSignatureException(
                "algorithms.WrongKeyForThisOperation",
                exArgs);
        }

        try
        {
            this.signatureAlgorithm.initVerify((PublicKey) gssKey);
        }
        catch(InvalidKeyException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }

    protected void engineInitSign(
        Key gssKey,
        AlgorithmParameterSpec algorithmParameterSpec) throws XMLSignatureException
    {
        throw new XMLSignatureException(
            "algorithms.CannotUseAlgorithmParameterSpecOnDSA");
    }

    protected void engineInitSign(Key gssKey) throws XMLSignatureException
    {
        if(!(gssKey instanceof GSSPrivateKey))
        {
            String supplied = gssKey.getClass().getName();
            String needed = GSSPrivateKey.class.getName();
            Object exArgs[] = {supplied, needed};

            throw new XMLSignatureException(
                "algorithms.WrongKeyForThisOperation",
                exArgs);
        }

        try
        {
            this.signatureAlgorithm.initSign((PrivateKey) gssKey);
        }
        catch(InvalidKeyException ex)
        {
            throw new XMLSignatureException("empty", ex);
        }
    }
}
