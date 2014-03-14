package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.*;
import java.lang.Exception;
import java.security.cert.X509Certificate;
import java.util.Map;
import org.globus.gsi.X509Credential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GlobusSecurityAdaptor implements ExpirableSecurityAdaptor {
    public static final int USAGE_INIT_PKCS12 = 1;
    public static final int USAGE_INIT_PEM = 2;
    public static final int USAGE_MEMORY = 3;
    public static final int USAGE_LOAD = 4;

    public abstract String getType();
    protected abstract int getGlobusType();
    protected abstract boolean checkType(GSSCredential proxy);

    public Class getSecurityCredentialClass() {
        return GlobusSecurityCredential.class;
    }

    protected Usage getPKCS12orPEM() {
        Usage PKCS12 = new UFile(USAGE_INIT_PKCS12, GlobusContext.USERCERTKEY);
        Usage PEM = new UAnd.Builder()
                            .id(USAGE_INIT_PEM)
                            .and(new UFile(Context.USERCERT))
                            .and(new UFile(Context.USERKEY))
                            .build();
        return new UAnd.Builder()
                                .and(new UOr.Builder().or(PKCS12).or(PEM).build())
                                .and(new UFilePath(Context.USERPROXY))
                                .and(new UHidden(Context.USERPASS))
                                .and(new UDuration(Context.LIFETIME))
                                .and(getDelegation())
                                .build();
    }
    
    protected Usage getDelegation() {
        return new UOptional(GlobusContext.DELEGATION) {
            protected Object throwExceptionIfInvalid(Object value) throws Exception {
                if (super.throwExceptionIfInvalid(value) != null) {
                    String v = (String) value;
                    if (!v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                        throw new BadParameterException("Expected: limited | full");
                    }
                }
                return value;
            }
        };
    }
    
    public Usage getUsage() {

        return new UAnd.Builder()
                .and(new UOr.Builder()
                        .or(new UNoPrompt(USAGE_MEMORY, GlobusContext.USERPROXYOBJECT))
                        .or(new UFile(USAGE_LOAD, Context.USERPROXY))
                        .or(getPKCS12orPEM())
                        .build()
                )
                .and(new UFile(Context.CERTREPOSITORY))
                .build();
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default(Context.USERPROXY, new String[]{
                        env.getProperty("X509_USER_PROXY"),
                        System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u"+
                                (System.getProperty("os.name").toLowerCase().startsWith("windows")
                                        ? "_"+System.getProperty("user.name").toLowerCase()
                                        : (env.getProperty("UID")!=null
                                                ? env.getProperty("UID")
                                                : getUnixUID()
                                          )
                                )}),
                new Default(Context.USERCERT, new File[]{
                        new File(env.getProperty("X509_USER_CERT")+""),
                        new File(System.getProperty("user.home")+"/.globus/usercert.pem")}),
                new Default(Context.USERKEY, new File[]{
                        new File(env.getProperty("X509_USER_KEY")+""),
                        new File(System.getProperty("user.home")+"/.globus/userkey.pem")}),
                new Default(Context.CERTREPOSITORY, new File[]{
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")}),
                new Default(Context.LIFETIME, "PT12H"),
                new Default(GlobusContext.DELEGATION, "full")
        };
    }
    protected static String getUnixUID() throws IncorrectStateException {
        try {
            Process p = Runtime.getRuntime().exec("id -u");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String uid = reader.readLine();
            reader.close();
            return uid;
        } catch (IOException e) {
            throw new IncorrectStateException(e);
        }
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        try {
            switch(usage) {
                case USAGE_INIT_PKCS12:
                {
                    GlobusProxyFactory factory = new GlobusProxyFactory(attributes, this.getGlobusType(), GlobusProxyFactory.CERTIFICATE_PKCS12);
                    GSSCredential cred = factory.createProxy();
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_INIT_PEM:
                {
                    GlobusProxyFactory factory = new GlobusProxyFactory(attributes, this.getGlobusType(), GlobusProxyFactory.CERTIFICATE_PEM);
                    GSSCredential cred = factory.createProxy();
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_MEMORY:
                {
                    String base64 = (String) attributes.get(GlobusContext.USERPROXYOBJECT);
                    GSSCredential cred = InMemoryProxySecurityCredential.toGSSCredential(base64);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_LOAD:
                {
                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
                    File proxyFile = new File((String) attributes.get(Context.USERPROXY));
                    GSSCredential cred = load(proxyFile);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                default:
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
            }
        } catch(IncorrectStateException e) {
            throw e;
        } catch(NoSuccessException e) {
            throw e;
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }
    private SecurityCredential createSecurityAdaptor(GSSCredential cred, Map attributes) throws IncorrectStateException {
        if (this.checkType(cred) && !hasNonCriticalExtensions(cred)) {
            File certRepository = new File((String) attributes.get(Context.CERTREPOSITORY));
            return new GlobusSecurityCredential(cred, certRepository);
        } else {
            throw new IncorrectStateException("Security context is not of type: "+this.getType());
        }
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
    }

    protected static GSSCredential load(File proxyFile) throws IOException, GSSException {
        byte [] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(
                proxyBytes,
                ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME,
                null, // use default mechanism: GSI
                GSSCredential.INITIATE_AND_ACCEPT);
    }

    private static boolean hasNonCriticalExtensions(GSSCredential proxy) {
        if (proxy instanceof GlobusGSSCredentialImpl) {
            X509Credential globusProxy = ((GlobusGSSCredentialImpl)proxy).getX509Credential();
            X509Certificate cert = globusProxy.getCertificateChain()[0];
            if (cert instanceof X509CertificateObject) {
                X509CertificateObject bouncyCert = (X509CertificateObject) cert;
                return bouncyCert.getNonCriticalExtensionOIDs()!=null && !bouncyCert.getNonCriticalExtensionOIDs().isEmpty();
            }
        }
        return false;
    }
}
