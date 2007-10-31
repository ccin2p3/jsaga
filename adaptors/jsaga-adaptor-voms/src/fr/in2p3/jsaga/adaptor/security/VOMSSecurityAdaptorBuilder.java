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
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.io.*;
import java.security.cert.X509Certificate;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSSecurityAdaptorBuilder
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   11 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class VOMSSecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    private static final Usage LOCAL_PROXY_OBJECT = new UNoPrompt("UserProxyObject");
    private static final Usage LOCAL_PROXY_FILE = new UFile("UserProxy");

    public String getType() {
        return "VOMS";
    }

    public Class getSecurityAdaptorClass() {
        return VOMSSecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE}),
                new UFile("CertDir"),
                new UFile("VomsDir")
        });
    }

    public Default[] getDefaults(Map map) throws IncorrectState {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default("UserProxy", new String[]{
                        env.getProperty("X509_USER_PROXY"),
                        System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u"+
                                (System.getProperty("os.name").toLowerCase().startsWith("windows")
                                        ? "_"+System.getProperty("user.name").toLowerCase()
                                        : (env.getProperty("UID")!=null
                                                ? env.getProperty("UID")
                                                : getUnixUID()
                                          )
                                )}),
                new Default("UserCert", new File[]{
                        new File(env.getProperty("X509_USER_CERT")+""),
                        new File(System.getProperty("user.home")+"/.globus/usercert.pem")}),
                new Default("UserKey", new File[]{
                        new File(env.getProperty("X509_USER_KEY")+""),
                        new File(System.getProperty("user.home")+"/.globus/userkey.pem")}),
                new Default("CertDir", new File[]{
                        new File(env.getProperty("X509_CERT_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
                        new File("/etc/grid-security/certificates/")}),
                new Default("VomsDir", new File[]{
                        new File(env.getProperty("X509_VOMS_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/vomsdir/"),
                        new File("/etc/grid-security/vomsdir/")}),
                new Default("LifeTime", "PT12H")
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
            String base64 = (String) attributes.get("UserProxyObject");
            cred = InMemoryProxySecurityAdaptor.toGSSCredential(base64);
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            CoGProperties.getDefault().setCaCertLocations((String) attributes.get("CertDir"));
            File proxyFile = new File((String) attributes.get("UserProxy"));
            cred = load(proxyFile);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
        // check if proxy contains extension
        if (hasNonCriticalExtensions(cred)) {
            return new VOMSSecurityAdaptor(cred);
        } else {
            throw new IncorrectState("Security context is not of type: "+this.getType());
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
