package org.glite.ce.commonj.certificate;

import java.util.List;

import org.glite.ce.commonj.certificate.ProxyCertificate.ProxyCertificateType;


public interface ProxyCertificateStorageInterface {
    public static final String PROXY_CERTIFICATE_DATABASE_NAME = "proxy_certificate_db";
    public static final String PROXY_CERTIFICATE_DATASOURCE_NAME = "java:comp/env/jdbc/proxy_certificate_db";
    
    public abstract void addProxyCertificateListener(ProxyCertificateListener listener);
        
    public void deleteProxyCertificate(String id, String userId) throws ProxyCertificateException, IllegalArgumentException;
    
    public void deleteProxyCertificate(String id, String DN, String FQAN) throws ProxyCertificateException, IllegalArgumentException;
    
    public ProxyCertificate getProxyCertificate(String id, String userId) throws ProxyCertificateException, IllegalArgumentException;

    public ProxyCertificate getProxyCertificate(String id, String DN, String FQAN) throws ProxyCertificateException, IllegalArgumentException;
    
    public List<ProxyCertificate> getProxyCertificateList() throws ProxyCertificateException, IllegalArgumentException;   
    
    public List<ProxyCertificate> getProxyCertificateList(String userDN) throws ProxyCertificateException, IllegalArgumentException;   

    public List<ProxyCertificate> getProxyCertificateList(String userDN, String FQAN, ProxyCertificateType proxyType) throws ProxyCertificateException, IllegalArgumentException;

    public abstract List<ProxyCertificateListener> getProxyCertificateListener();

    public abstract void removeProxyCertificateListener(ProxyCertificateListener listener);
    
    public void setProxyCertificate(ProxyCertificate proxyCertificate) throws ProxyCertificateException, IllegalArgumentException;
}
