package fr.in2p3.jsaga.adaptor.cream;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.globus.gsi.CredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.AuthenticationFailedException;

import eu.emi.security.authn.x509.X509Credential;
import eu.emi.security.authn.x509.helpers.proxy.ProxyCertificateImpl;
import eu.emi.security.authn.x509.impl.OpensslCertChainValidator;
import eu.emi.security.authn.x509.impl.PEMCredential;
import eu.emi.security.authn.x509.impl.SocketFactoryCreator;

public class CreamSocketFactory implements SecureProtocolSocketFactory {

    private X509Credential m_credential;
    private OpensslCertChainValidator m_validator;
    
    public CreamSocketFactory(GSSCredential cred, File certificatesPath) throws AuthenticationFailedException {
        try {
            org.globus.gsi.X509Credential c = ((GlobusGSSCredentialImpl)cred).getX509Credential();
            m_credential = new ProxyCertificateImpl(c.getCertificateChain(), c.getPrivateKey()).getCredential();
        } catch (KeyStoreException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        } catch (IllegalStateException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        } catch (CredentialException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        }
        m_validator = new OpensslCertChainValidator(certificatesPath.getPath());
    }

    public CreamSocketFactory(String credFile, File certificatesPath) throws AuthenticationFailedException {
        try {
            m_credential = new PEMCredential(credFile, (char[])null);
        } catch (KeyStoreException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        } catch (CertificateException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        } catch (IOException e1) {
            throw new AuthenticationFailedException("Error with proxy: " + e1.getMessage(),e1);
        }
        m_validator = new OpensslCertChainValidator(certificatesPath.getPath());
    }
    
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return createSocket(host, port, null, 0);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return createSocket(host, port, localHost, localPort, null);
    }

    public Socket createSocket(String host, int port, InetAddress localHost,
            int localPort, HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        SSLSocketFactory newFactory = SocketFactoryCreator.getSocketFactory(m_credential, m_validator);
        SSLSocket socket = (SSLSocket) newFactory.createSocket();
        SocketAddress remoteaddr = new InetSocketAddress(host, port);
        if (params != null) {
            socket.setSoTimeout(params.getConnectionTimeout());
        }
        if (localHost != null && localPort != 0) {
            socket.bind(new InetSocketAddress(localHost, localPort));
        }
        socket.connect(remoteaddr);
        return socket;
    }

    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        return null;
    }

}
