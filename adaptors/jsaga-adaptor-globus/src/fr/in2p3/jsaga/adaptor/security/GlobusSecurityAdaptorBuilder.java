package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.globus.common.CoGProperties;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.*;
import org.ogf.saga.context.Context;

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
public abstract class GlobusSecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    private static final Usage LOCAL_PROXY_OBJECT = new UNoPrompt(GlobusContext.USERPROXYOBJECT);
    private static final Usage LOCAL_PROXY_FILE = new UFile(Context.USERPROXY);

    public abstract String getType();
    public abstract boolean checkType(GSSCredential proxy);

    public Class getSecurityAdaptorClass() {
        return GlobusSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE}),
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
                new Default(GlobusContext.USERCERTKEY, new File[]{
                        new File(env.getProperty("PKCS12_USER_CERT")+""),
                        new File(System.getProperty("user.home")+"/.globus/usercert.p12")}),
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

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        GSSCredential cred;
        if (LOCAL_PROXY_OBJECT.getMissingValues(attributes) == null) {
            String base64 = (String) attributes.get(GlobusContext.USERPROXYOBJECT);
            cred = InMemoryProxySecurityAdaptor.toGSSCredential(base64);
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
            File proxyFile = new File((String) attributes.get(Context.USERPROXY));
            cred = load(proxyFile);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
        if (this.checkType(cred) && !hasNonCriticalExtensions(cred)) {
            return new GlobusSecurityAdaptor(cred);
        } else {
            throw new NoSuccess("Security context is not of type: "+this.getType());
        }
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
