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
package org.globus.wsrf.impl.security.authentication.wssec;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.ws.patched.security.WSSConfig;
import org.apache.ws.patched.security.message.EnvelopeIdResolver;
import org.apache.ws.patched.security.message.token.SecurityTokenReference;
import org.apache.ws.patched.security.message.token.Timestamp;
import org.apache.ws.patched.security.message.token.UsernameToken;
import org.apache.ws.patched.security.util.WSSecurityUtil;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.security.utils.IdResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.MessageID;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSCredential;
import org.gridforum.jgss.ExtendedGSSContext;

import org.globus.gsi.CertUtil;
import org.globus.gsi.CertificateRevocationLists;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.jaas.GlobusPrincipal;
import org.globus.gsi.jaas.PasswordCredential;
import org.globus.gsi.jaas.UserNamePrincipal;
import org.globus.gsi.proxy.ProxyPathValidator;
import org.globus.util.I18n;
import org.globus.wsrf.NoResourceHomeException;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authentication.ContextCrypto;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.authentication.signature.SignatureGSS;
import org.globus.wsrf.impl.security.descriptor.SecurityPropertiesHelper;
import org.globus.wsrf.impl.security.util.EnvelopeConverter;
import org.globus.wsrf.utils.ContextUtils;
import org.globus.wsrf.utils.XmlUtils;

public abstract class WSSecurityEngine {

    protected static I18n i18n = I18n
        .getI18n("org.globus.wsrf.impl.security.authentication.wssec.errors");
    private static Log log =
        LogFactory.getLog(WSSecurityEngine.class.getName());

    public static final String SIG_LN = "Signature";

    private org.apache.ws.patched.security.WSSecurityEngine wssEngine;

    private static ContextCrypto crypto = ContextCrypto.getInstance();

    public boolean handleSignatureElement(Element element,
                                          MessageContext msgCtx,
                                          boolean request)
        throws Exception {

        normalize(element);

        XMLSignature sig = new XMLSignature(element, null);
        sig.addResourceResolver(EnvelopeIdResolver.getInstance(
                                    WSSConfig.getDefaultWSConfig()));

        SignedInfo info = sig.getSignedInfo();

        boolean sigValid = false;
        if (info.getSignatureMethodURI().equalsIgnoreCase(SignatureGSS.URI)) {
            log.debug("Found GSS XML signature");
            sigValid = verifyGssXMLSignature(sig, msgCtx);
        } else {
            log.debug("Found XML signature");
            sigValid = verifyXMLSignature(sig, msgCtx);
        }

        // If signature is valid, enforce that dispactch headers are secure
        if (sigValid) {
            enforceSecureDispatchHeaders(info, msgCtx, request);
        }
        return sigValid;
    }

    protected void enforceSecureDispatchHeaders(SignedInfo info,
                                                MessageContext msgCtx,
                                                boolean request)
        throws Exception {

        // Construct a HashMap of the QName of referenced elements
        int referenceLength = info.getLength();
        HashMap signedQName = new HashMap();
        for (int i=0; i<referenceLength; i++) {
            org.apache.xml.security.signature.Reference ref = info.item(i);
            log.debug("Reference URI " + ref.getURI());
            // FIXME: Revisit wrt performance.
            XMLSignatureInput input = ref.getContentsBeforeTransformation();
            Set nodeSet = input.getNodeSet();
            Iterator iter = nodeSet.iterator();
            while (iter.hasNext()) {
                Node node = (Node)iter.next();
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    QName qName = new QName(node.getNamespaceURI(),
                                            node.getLocalName());
                    log.debug("Adding Qname " + qName);
                    signedQName.put(qName, "");
                }
            }
        }

        // If request, walk through the required list and ensure the above hash
        // map contains it.
        if (request) {
            HashMap enforceQName =
                (HashMap)msgCtx.getProperty(Constants.ENFORCED_SECURE_HEADERS);

            // need to add resource key header, if present
            QName keyName = getResourceKeyHeaderQName(msgCtx);
            log.debug("Key name " + keyName);
            if (keyName != null) {
                if (enforceQName == null) {
                    enforceQName = new HashMap();
                }
                enforceQName.put(keyName, "");
            }

            if (enforceQName != null) {
                Iterator keysIterator = enforceQName.keySet().iterator();
                while (keysIterator.hasNext()) {
                    QName headerQName = (QName)keysIterator.next();
                    if (!signedQName.containsKey(headerQName)) {
                        throw new WSSecurityException(WSSecurityException
                                                      .FAILURE,
                                                      "insecureHeader",
                                                      new Object[] {
                                                          headerQName });
                    }
                }
            }

        }
    }

    protected QName getResourceKeyHeaderQName(MessageContext msgCtx)
        throws Exception {
        QName keyName = null;
        ResourceContext resCtx = ResourceContext
            .getResourceContext((org.apache.axis.MessageContext)msgCtx);
        SOAPHeaderElement keyHeader = null;
        try {
            keyHeader = resCtx.getResourceKeyHeader();
        } catch (NoResourceHomeException exp) {
            // quite catch.
            log.debug(exp);
        }
        if (keyHeader != null) {
            Name name = keyHeader.getElementName();
            keyName = new QName(name.getURI(), name.getLocalName());
        }
        return keyName;
    }

    public abstract boolean verifyGssXMLSignature(XMLSignature sig,
                                                  MessageContext msgCtx)
        throws Exception;

    protected X509Certificate[] getCertificatesX509Data(KeyInfo info)
        throws Exception {
        int len = info.lengthX509Data();

        if (len != 1) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "invalidX509Data",
                                          new Object[] { new Integer(len) });
        }

        X509Data data = info.itemX509Data(0);
        int certLen = data.lengthCertificate();

        if (certLen <= 0) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "invalidCertData",
                new Object[] { new Integer(certLen) }
            );
        }

        X509Certificate[] certs = new X509Certificate[certLen];
        XMLX509Certificate xmlCert;
        ByteArrayInputStream input;

        for (int i = 0; i < certLen; i++) {
            xmlCert = data.itemCertificate(i);
            input = new ByteArrayInputStream(xmlCert.getCertificateBytes());
            certs[i] = CertUtil.loadCertificate(input);
        }

        return certs;
    }

    public abstract boolean verifyXMLSignature(XMLSignature sig,
                                               MessageContext msgCtx)
        throws Exception;

    protected boolean verifyXMLSignature(XMLSignature sig,
                                         MessageContext msgCtx,
                                         ProxyPathValidator validator)
        throws Exception {
        log.debug("Verify XML Signature");

        X509Certificate[] certs = null;
        KeyInfo info = sig.getKeyInfo();

        if (info.containsX509Data()) {
            certs = getCertificatesX509Data(info);
        } else {
            Node node =
                WSSecurityUtil.getDirectChild(
                    (Element)info.getElement(),
                    SecurityTokenReference.SECURITY_TOKEN_REFERENCE,
                    WSSConfig.getDefaultWSConfig().getWsseNS()
                );
            if (node == null) {
                throw new WSSecurityException(
                    WSSecurityException.INVALID_SECURITY,
                    "unsupportedKeyInfo", null);
            } else {
                String uri =
                    ((Element)node.getFirstChild()).getAttribute("URI");
                Element tokElement = org.apache.ws.patched.security.util.WSSecurityUtil.getElementByWsuId(WSSConfig.getDefaultWSConfig(),node.getOwnerDocument(), uri);
                certs = org.apache.ws.patched.security.WSSecurityEngine.getInstance().getCertificatesTokenReference(tokElement,
                                                                                                            crypto);
            }
        }

        if (!sig.checkSignatureValue(certs[0])) {
            throw new WSSecurityException(WSSecurityException.FAILED_CHECK);
        }

        // TODO: in the future we can get it from context too.
        X509Certificate[] trustedCerts = null;

        TrustedCertificates crts =
            TrustedCertificates.getDefaultTrustedCertificates();

        if (crts != null) {
            trustedCerts = crts.getCertificates();
        }

        // Get CRLs
        CertificateRevocationLists defaultCrls =
            CertificateRevocationLists.getDefaultCertificateRevocationLists();

        if (log.isDebugEnabled()) {
            for (int i = 0; i < certs.length; i++) {
                log.debug("Cert " + certs[i].getSubjectDN().getName());
            }
        }
        validator.validate(certs, trustedCerts, defaultCrls);

        // get the identity
        String identity = validator.getIdentity();

        msgCtx.setProperty(Constants.GSI_SEC_MSG, Constants.SIGNATURE);

        // fill in the JAAS Subject
        Subject subject = getSubject(msgCtx);
        subject.getPublicCredentials().add(certs);
        subject.getPrincipals().add(new GlobusPrincipal(identity));

        return true;
    }

    protected Subject getSubject(MessageContext msgCtx) {

        Subject subject = (Subject) msgCtx.getProperty(Constants.PEER_SUBJECT);

        if (subject == null) {
            subject = new Subject();
            msgCtx.setProperty(Constants.PEER_SUBJECT, subject);
        }
        return subject;
    }

    public boolean handleEncryptionElement(Element element,
                                           MessageContext msgCtx)
        throws Exception {

        if (element.getLocalName().equals(
            EncryptionConstants._TAG_ENCRYPTEDKEY)) {

                log.debug("Found XML Encryption");
                return decryptXMLEncryption(element, msgCtx);

        } else {
            Element tmpE = (Element)org.apache.ws.patched.security.util.WSSecurityUtil
                .findElement(element.getOwnerDocument(),
                             "EncryptionMethod",
                             org.apache.ws.patched.security.WSConstants.ENC_NS);

            tmpE = (Element)org.apache.ws.patched.security.util.WSSecurityUtil
                .findElement(element, "DataReference",
                             org.apache.ws.patched.security.WSConstants.ENC_NS);
            String uri = tmpE.getAttribute("URI");
            Element encryptedDataElem =
                IdResolver.getElementById(element.getOwnerDocument(),
                                          uri.substring(1));
            if (encryptedDataElem == null) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "noEncryptedData",
                    new Object[] { tmpE.getAttribute("URI") }
                );
            }
            wssEngine.decryptDataRefEmbedded(
                encryptedDataElem.getOwnerDocument(), uri,
                new WSSecurityCallbackHandler(
                    (org.apache.axis.MessageContext) msgCtx));
            setContextProperties(msgCtx,
                                 (SecurityContext) msgCtx.getProperty(
                                     Constants.CONTEXT),
                                 Constants.ENCRYPTION);
            return true;
        }
    }

    public abstract boolean decryptXMLEncryption(Element element,
                                                 MessageContext msgCtx)
        throws Exception;

    public boolean decryptXMLEncryption(Element element,
                                        PrivateKey privateKey)
        throws Exception {
        wssEngine.handleEncryptedKey(element, privateKey);
        Document doc = element.getOwnerDocument();
        Element soapBodyElement = WSSecurityUtil
            .findBodyElement(
                doc, WSSecurityUtil.getSOAPConstants(doc.getDocumentElement()));
        Element bodyElement = XmlUtils.getFirstChildElement(soapBodyElement);
        bodyElement.removeAttributeNS("http://www.w3.org/2000/xmlns/", "xenc");
        log.debug("Exit: decryptXMLEncryption");
        return true; // DOM modified
    }

    public abstract Document processSecurityHeader(SOAPEnvelope env,
                                                   MessageContext msgCtx)
        throws Exception;

    // If request is true, then timestamp header is processed (and
    // replay attack prevention kicks in) and also resource key header
    // is added to the list of dispatch headers that are required to
    // be secured.
    public Document processSecurityHeader(SOAPEnvelope env,
                                          MessageContext msgCtx,
                                          boolean request)
        throws Exception {

        return processSecurityHeader(env,
                                     (String) msgCtx.getProperty("actor"),
                                     msgCtx, request);
    }

    public Document processSecurityHeader(SOAPEnvelope env, String actor,
                                          MessageContext msgCtx,
                                          boolean request)
        throws Exception {

        if (actor == null) {
            actor = "";
        }

        // checks if there are any WS-Security headers
        SOAPHeaderElement securityHeader = null;
        SOAPHeaderElement messageIDHeader = null;

        SOAPHeader header = env.getHeader();

        // No headers, return
        if (header == null) {
            return null;
        }

        Iterator headerElements = header.examineHeaderElements(actor);
        int headerCnt = 0;
        while (headerElements.hasNext()) {
            SOAPHeaderElement he = (SOAPHeaderElement) headerElements.next();
            Name nm = he.getElementName();

            // find ws-security header
            if (nm.getLocalName().equalsIgnoreCase(WSConstants.WS_SEC_LN) &&
                nm.getURI().equalsIgnoreCase(WSConstants.WSSE_NS)) {
                securityHeader = he;
                headerCnt++;
            } else if (nm.getLocalName()
                       .equalsIgnoreCase(org.apache.axis.message.addressing
                                         .Constants.MESSAGE_ID) &&
                       nm.getURI()
                       .equalsIgnoreCase(org.apache.axis.message.addressing
                                         .Constants.NS_URI_ADDRESSING)) {
                messageIDHeader = he;
                headerCnt++;
            }
            if (headerCnt == 2) {
                break;
            }
        }

        if (securityHeader == null) {
            return null;
        }

        GSSConfig.init();

        if (wssEngine == null) {
            wssEngine = GSSSecurityEngine.getInstance();
        }

        // convert env to DOM
        Document doc = EnvelopeConverter.getInstance().toDocument(env);

        // get security header and iterator thro' WS-Security element in it
        NodeList list = doc
            .getElementsByTagNameNS(org.apache.ws.patched.security.WSConstants.WSSE_NS,
                                    WSConstants.WS_SEC_LN);
        int len = list.getLength();
        Element elem;
        Attr attr;
        String headerActor = null;

        for (int i = 0; i < len; i++) {
            elem = (Element) list.item(i);
            attr = elem.getAttributeNodeNS(WSConstants.SOAP_NS, "actor");
            if (attr != null) {
                headerActor = attr.getValue();
            }
            if ((headerActor == null) || (headerActor.length() == 0) ||
                headerActor.equalsIgnoreCase(actor) ||
                // FIXME - pull out string as a constant.
                headerActor.equals(
                    "http://schemas.xmlsoap.org/soap/actor/next"
                )) {

                processSecurityHeader(elem, msgCtx, headerActor,
                                      messageIDHeader, request);
            }
        }

        return doc;
    }

    public void processSecurityHeader(Element securityHeader,
                                      MessageContext msgCtx, String actor,
                                      SOAPHeaderElement messageIdHeader,
                                      boolean request)
        throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Processing WS-Security header for '" + actor + "' actor."
                      + " request (so process timestamp) " + request);
        }
        // Get timestamp header if it needed and Normalize it.
        Element timestampElem = null;

        NodeList list = securityHeader.getChildNodes();
        int len = list.getLength();
        String ns = null;
        String ln = null;
        Node elem;
        for (int i = 0; i < len; i++) {
            elem = list.item(i);
            ns = elem.getNamespaceURI();
            ln = elem.getLocalName();
            if (WSConstants.SIG_NS.equalsIgnoreCase(ns) &&
                SIG_LN.equalsIgnoreCase(ln)) {
                // found SignedInfo element
                log.debug("Found signature element");
                if(handleSignatureElement((Element) elem, msgCtx, request)
                   == false) {
                    //TODO: better exception
                    throw new WSSecurityException(
                        WSSecurityException.FAILED_CHECK);
                }
            } else if (WSConstants.ENC_NS.equalsIgnoreCase(ns)) {
                // found _some_ element in xml encryption namespace
                log.debug("Found encryption element");
                handleEncryptionElement((Element) elem, msgCtx);
                // FIXME: shoudl ideally check for NS too, but
                // username ns is not used by WSS4J - need to confirm
            } else if (org.apache.ws.patched.security.WSConstants.USERNAME_TOKEN_LN.
                       equalsIgnoreCase(ln)) {
                log.debug("Found user name token");
                handleUsernameElement((Element)elem, msgCtx);
            } else if (WSConstants.WSU_NS.equalsIgnoreCase(ns) &&
                       WSConstants.WS_SEC_TS_LN.equalsIgnoreCase(ln)) {
                log.debug("Found timestamp element");
                timestampElem = (Element)elem;
                normalize(timestampElem);
            } else if (elem.getNodeType() == Node.ELEMENT_NODE) {
                log.debug(elem.getLocalName() + " " + elem.getNamespaceURI());
            }
        }

        // At this point signature has been processed. Handle
        // timestamp, if need be.
        if (request) {
            log.debug("Secure message, timestamp might be required");
            if ((constantSet(msgCtx.getProperty(Constants.GSI_SEC_MSG),
                             Constants.SIGNATURE)) ||
                (constantSet(msgCtx.getProperty(Constants.GSI_SEC_MSG),
                             Constants.ENCRYPTION))) {
                processTimestampHeader(timestampElem, msgCtx, messageIdHeader);
            }
        }

        boolean route =
            ("".equals(actor) &&
             Boolean.TRUE.equals(msgCtx.getProperty(Constants.ROUTED)));

        // delete processed header
        if (!route) {
            securityHeader.getParentNode().removeChild(securityHeader);
        } else {
            log.debug("Header not removed");
        }
    }

    public boolean handleUsernameElement(Element element,
                                         MessageContext msgCtx)
        throws Exception {
        log.debug("User name processing");

        UsernameToken userNameTok =
            new UsernameToken(WSSConfig.getDefaultWSConfig(), element);


        // FIXME: Check if message is valid - nonce or timestamp
        // get username and password (if present)
        String userName = userNameTok.getName();
        String password = userNameTok.getPassword();

        Subject subject = getSubject(msgCtx);
        subject.getPrincipals().add(new UserNamePrincipal(userName));
        if (password != null) {
            subject.getPrivateCredentials()
                .add(new PasswordCredential(password));
        }

        // set username stuff on msg context, for authz purposes.
        msgCtx.setProperty(org.globus.wsrf.impl.security.authentication
                           .Constants.USERNAME_AUTH, Boolean.TRUE);
        // DOM unchanged.
        return false;
    }

    // If secure message is used, then a timestamp is required.
    protected void processTimestampHeader(Element timestampElem,
                                          MessageContext msgCtx,
                                          SOAPHeaderElement messageIDHeader)
        throws Exception {

        String servicePath = ContextUtils
            .getTargetServicePath((org.apache.axis.MessageContext)msgCtx);
        if (servicePath == null) {
            throw new Exception(i18n.getMessage("serviceNull"));
        }

        Resource resource = null;
        try {
            ResourceContext resCtx = ResourceContext
                .getResourceContext((org.apache.axis.MessageContext)msgCtx);
            resource = resCtx.getResource();
        } catch (ResourceContextException exp) {
            // FIXME quiet catch
            log.debug("Resource does not exist ", exp);
        } catch (ResourceException exp) {
            // FIXME quiet catch
            log.debug("Resource does not exist ", exp);
        }

        if (timestampElem != null) {

            String propertyValue =
                SecurityPropertiesHelper.getReplayAttackWindow(servicePath,
                                                               resource);
            ReplayAttackFilter replayFilter =
                ReplayAttackFilter.getInstance(propertyValue);

            if (messageIDHeader == null) {
                Timestamp timestamp =
                    new Timestamp(WSSConfig.getDefaultWSConfig(),
                                  timestampElem);
                boolean stampOk =
                    verifyTimestamp(timestamp,
                                    replayFilter.getMessageWindow());
                if (!stampOk) {
                    throw new WSSecurityException(WSSecurityException.FAILURE,
                                                  "timestampNotOk");
                }
            } else {
                checkMessageValidity(replayFilter, timestampElem,
                                     messageIDHeader);
            }
        } else {
            String propertyValue =
                SecurityPropertiesHelper.getReplayAttackFilter(servicePath,
                                                               resource);
            if (rejectMsgSansTimestampHeader(msgCtx, propertyValue)) {
                log.debug("Required time stamp header was not added.");
                throw new WSSecurityException(WSSecurityException.FAILURE,
                                              "timestampRequired");
            }
        }
        log.debug("Done processing timestamp header.");
    }

    protected boolean verifyTimestamp(Timestamp timestamp, int TTL) {
        return verifyTimestamp(timestamp.getCreated(), TTL);
    }

    protected boolean verifyTimestamp(Calendar created, int TTL) {
        // Calculate the time that is allowed for the message to travel
        Calendar validCreation = Calendar.getInstance();
        long currentTime = validCreation.getTime().getTime();
        currentTime -= TTL * 1000;
        validCreation.setTime(new Date(currentTime));
        if (!created.after(validCreation)) {
                        return false;
        }
        return true;
    }

    protected void checkMessageValidity(ReplayAttackFilter replayFilter,
                                        Element timestampElem,
                                        SOAPHeaderElement messageIDHeader)
        throws Exception {

        Timestamp timeStamp = new Timestamp(WSSConfig.getDefaultWSConfig(),
                                            timestampElem);
        MessageID messageId = new MessageID(messageIDHeader);
        replayFilter.checkMessageValidity(messageId.toString(),
                                          timeStamp.getCreated());
    }

    protected boolean rejectMsgSansTimestampHeader(MessageContext msgCtx,
                                                   String propertyValue)
        throws Exception {

        if ((propertyValue != null) && (propertyValue.equals("false"))) {
            return false;
        }

        return true;
    }

    /**
     * Replaces all Text nodes that start with "\n " or "\n\n" with
     * "\n" This is only used by signature callback.
     * There is a bug somewhere in serliazation/deserialization code
     * that appends spaces to \n Text nodes for no reason breaking
     * the signature stuff.
     */
    public static void normalize(Node node) {

        if (node.getNodeType() == Node.TEXT_NODE) {
            String data = ((Text) node).getData();
            if (
                (data.length() > 1) && (data.charAt(0) == 10) &&
                ((data.charAt(1) == 10) || (data.charAt(1) == 32))
            ) {
                ((Text) node).setData("\n");
            }
        }

        for (
            Node currentChild = node.getFirstChild(); currentChild != null;
            currentChild = currentChild.getNextSibling()
            ) {
                normalize(currentChild);
            }
    }

    protected void ensureSignature(MessageContext msgCtx) throws Exception {

        Object obj = msgCtx.getProperty(Constants.GSI_SEC_MSG);
        if ((obj == null) || (!(obj.equals(Constants.SIGNATURE)))) {
            throw new WSSecurityException(WSSecurityException.FAILURE,
                                          "encRequiresSig");
        }
    }

    private boolean constantSet(Object msgVal, Object propValue) {
        if ((msgVal != null) && (msgVal.equals(propValue)))
            return true;
        return false;
    }

    protected void setContextProperties(MessageContext msgContext,
                                        SecurityContext secContext,
                                        Integer msgType) throws Exception {
        // pass the security context to other handlers
        msgContext.setProperty(Constants.CONTEXT,
                               secContext);

        // pass the protection type to other handlers
        msgContext.setProperty(Constants.GSI_SEC_CONV,
                               msgType);

        // create subject
        Subject subject = getSubject(msgContext);
        GSSContext ctx = secContext.getContext();

        GSSName caller = ctx.getSrcName();

        // if caller is anonymous the Subject object will be empty
        // authorization should fail
        if (!caller.isAnonymous()) {
            String callerIdentity = caller.toString();
            GSSCredential cred = ctx.getDelegCred();

            if (ctx instanceof ExtendedGSSContext) {
                ExtendedGSSContext extGss = (ExtendedGSSContext) ctx;
                X509Certificate[] certs =
                    (X509Certificate[]) extGss
                    .inquireByOid(GSSConstants.X509_CERT_CHAIN);
                if (certs != null) {
                    subject.getPublicCredentials().add(certs);
                }
            }
            // fill in the Subject
            subject.getPrincipals().add(new GlobusPrincipal(callerIdentity));

            if (cred != null) {
                subject.getPrivateCredentials().add(cred);
            }
        }
    }
}
