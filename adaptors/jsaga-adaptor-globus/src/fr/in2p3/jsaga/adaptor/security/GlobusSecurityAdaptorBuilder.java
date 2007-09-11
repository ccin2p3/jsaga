package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyFile;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyObject;
import org.globus.common.CoGProperties;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.io.*;
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
public class GlobusSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final int MIN_REMAINING_LIFETIME = 30*60;    // 30 minutes

    private static final Usage LOCAL_PROXY_OBJECT = new UProxyObject("UserProxyObject", MIN_REMAINING_LIFETIME);
    private static final Usage LOCAL_PROXY_FILE = new UProxyFile("UserProxy", MIN_REMAINING_LIFETIME);
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"),
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
            new UOptional("Delegation") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                            throw new BadParameter("Expected: limited | full");
                        }
                    }
                    return value;
                }
            },
            new UOptional("ProxyType") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC820")) {
                            throw new BadParameter("Expected: old | globus | RFC820");
                        }
                    }
                    return value;
                }
            }
    });

    public String getType() {
        return "Globus";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE, CREATE_PROXY}),
                new UFile("CertDir")
        });
    }

    public Default[] getDefaults(Map map) throws IncorrectState {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
        return new Default[]{
                new Default("UserProxy", new String[]{
                        env.getProperty("X509_USER_PROXY"),
                        System.getProperty("os.name").toLowerCase().startsWith("windows")
                                ? System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u_"+System.getProperty("user.name").toLowerCase()
                                : System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u_"+env.getProperty("UID")}),
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
                new Default("LifeTime", "PT12H"),
                new Default("Delegation", "full")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        if (LOCAL_PROXY_OBJECT.getMissingValues(attributes) == null) {
            return new GlobusSecurityAdaptor((GSSCredential) attributes.get("UserProxyObject"));
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            CoGProperties.getDefault().setCaCertLocations((String) attributes.get("CertDir"));
            File proxyFile = new File((String) attributes.get("UserProxy"));
            return new GlobusSecurityAdaptor(load(proxyFile));
        } else if (CREATE_PROXY.getMissingValues(attributes) == null) {
            return this.initAndCreateSecurityAdaptor(attributes);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    public Usage getInitUsage() {
        return new UAnd(new Usage[]{CREATE_PROXY, new UFile("CertDir")});
    }

    public SecurityAdaptor initAndCreateSecurityAdaptor(Map attributes) throws Exception {
        return new GlobusSecurityAdaptor(new GlobusProxyFactory(attributes).createProxy());
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
}
