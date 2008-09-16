package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;
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

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusSecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class GlobusSecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
    private static final int USAGE_INIT_PKCS12 = 1;
    private static final int USAGE_INIT_PEM = 2;
    private static final int USAGE_MEMORY = 3;
    private static final int USAGE_LOAD = 4;

    public abstract String getType();
    protected abstract int getGlobusType();
    protected abstract boolean checkType(GSSCredential proxy);

    public Class getSecurityAdaptorClass() {
        return GlobusSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        new UAnd(new Usage[]{
                                new UOr(new Usage[]{
                                        new UFile(USAGE_INIT_PKCS12, GlobusContext.USERCERTKEY),
                                        new UAnd(USAGE_INIT_PEM, new Usage[]{new UFile(Context.USERCERT), new UFile(Context.USERKEY)})
                                }),
                                new UFilePath(Context.USERPROXY), new UHidden(Context.USERPASS),
                                new UDuration(Context.LIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                },
                                new UOptional(GlobusContext.DELEGATION) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        if (super.throwExceptionIfInvalid(value) != null) {
                                            String v = (String) value;
                                            if (!v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                                                throw new BadParameter("Expected: limited | full");
                                            }
                                        }
                                        return value;
                                    }
                                }
                        }),
                        new UNoPrompt(USAGE_MEMORY, GlobusContext.USERPROXYOBJECT),
                        new UFile(USAGE_LOAD, Context.USERPROXY)
                }),
                new UFile(Context.CERTREPOSITORY)
        });
    }

    public Default[] getDefaults(Map map) throws IncorrectState {
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
    protected static String getUnixUID() throws IncorrectState {
        try {
            Process p = Runtime.getRuntime().exec("id -u");
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String uid = reader.readLine();
            reader.close();
            return uid;
        } catch (IOException e) {
            throw new IncorrectState(e);
        }
    }

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectState, NoSuccess {
        try {
            switch(usage) {
                case USAGE_INIT_PKCS12:
                {
                    GlobusProxyFactory factory = new GlobusProxyFactory(attributes, this.getGlobusType(), GlobusProxyFactory.CERTIFICATE_PKCS12);
                    GSSCredential cred = factory.createProxy();
                    return this.createSecurityAdaptor(cred);
                }
                case USAGE_INIT_PEM:
                {
                    GlobusProxyFactory factory = new GlobusProxyFactory(attributes, this.getGlobusType(), GlobusProxyFactory.CERTIFICATE_PEM);
                    GSSCredential cred = factory.createProxy();
                    return this.createSecurityAdaptor(cred);
                }
                case USAGE_MEMORY:
                {
                    String base64 = (String) attributes.get(GlobusContext.USERPROXYOBJECT);
                    GSSCredential cred = InMemoryProxySecurityAdaptor.toGSSCredential(base64);
                    return this.createSecurityAdaptor(cred);
                }
                case USAGE_LOAD:
                {
                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
                    File proxyFile = new File((String) attributes.get(Context.USERPROXY));
                    GSSCredential cred = load(proxyFile);
                    return this.createSecurityAdaptor(cred);
                }
                default:
                    throw new NoSuccess("INTERNAL ERROR: unexpected exception");
            }
        } catch(IncorrectState e) {
            throw e;
        } catch(NoSuccess e) {
            throw e;
        } catch(Exception e) {
            throw new NoSuccess(e);
        }
    }
    private SecurityAdaptor createSecurityAdaptor(GSSCredential cred) throws IncorrectState {
        if (this.checkType(cred) && !hasNonCriticalExtensions(cred)) {
            return new GlobusSecurityAdaptor(cred);
        } else {
            throw new IncorrectState("Security context is not of type: "+this.getType());
        }
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
    }

    private static GSSCredential load(File proxyFile) throws IOException, GSSException {
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
            GlobusCredential globusProxy = ((GlobusGSSCredentialImpl)proxy).getGlobusCredential();
            X509Certificate cert = globusProxy.getCertificateChain()[0];
            if (cert instanceof X509CertificateObject) {
                X509CertificateObject bouncyCert = (X509CertificateObject) cert;
                return bouncyCert.getNonCriticalExtensionOIDs()!=null && !bouncyCert.getNonCriticalExtensionOIDs().isEmpty();
            }
        }
        return false;
    }
}
