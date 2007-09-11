package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import org.glite.security.voms.contact.*;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
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
public class VOMSSecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final int MIN_REMAINING_LIFETIME = 30*60;    // 30 minutes

    private static final Usage LOCAL_PROXY_OBJECT = new UNoPrompt("UserProxyObject") {
        public String toString() {return "_"+m_name+":"+MIN_REMAINING_LIFETIME+"_";}
        protected Object throwExceptionIfInvalid(Object value) throws Exception {
            GSSCredential cred = (GSSCredential) super.throwExceptionIfInvalid(value);
            if (cred.getRemainingLifetime() < MIN_REMAINING_LIFETIME) {
                throw new IncorrectState("Proxy file remaining lifetime if not enougth: "+cred.getRemainingLifetime());
            }
            return cred;
        }
    };
    private static final Usage LOCAL_PROXY_FILE = new UFile("UserProxy") {
        public String toString() {return "<"+m_name+":"+MIN_REMAINING_LIFETIME+">";}
        protected Object throwExceptionIfInvalid(Object value) throws Exception {
            File file = (File) super.throwExceptionIfInvalid(value); 
            GSSCredential cred = load(file);
            if (cred.getRemainingLifetime() < MIN_REMAINING_LIFETIME) {
                throw new IncorrectState("Proxy file remaining lifetime if not enougth: "+cred.getRemainingLifetime());
            }
            return cred;
        }
    };
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"),
            new U("Server"), new U("UserVO"), new UOptional("UserFQAN"),
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
            new UOptional("Delegation") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    if (super.throwExceptionIfInvalid(value) != null) {
                        String v = (String) value;
                        if (!v.equalsIgnoreCase("none") && !v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                            throw new BadParameter("Expected: none | limited | full");
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
        return "VOMS";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE, CREATE_PROXY}),
                new UFile("CertDir"),
                new UFile("VomsDir")
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
                new Default("VomsDir", new File[]{
                        new File(env.getProperty("X509_VOMS_DIR")+""),
                        new File(System.getProperty("user.home")+"/.globus/vomsdir/"),
                        new File("/etc/grid-security/vomsdir/")}),
                new Default("LifeTime", "PT12H")
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        if (LOCAL_PROXY_OBJECT.getMissingValues(attributes) == null) {
            return new VOMSSecurityAdaptor((GlobusCredential) attributes.get("UserProxyObject"));
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            File proxyFile = new File((String) attributes.get("UserProxy"));
            GSSCredential cred = load(proxyFile);
            if (cred instanceof GlobusGSSCredentialImpl) {
                return new VOMSSecurityAdaptor(((GlobusGSSCredentialImpl)cred).getGlobusCredential());
            } else {
                throw new BadParameter("Unexpected credential type: "+cred.getClass().getName());
            }
        } else if (CREATE_PROXY.getMissingValues(attributes) == null) {
            return this.initAndCreateSecurityAdaptor(attributes);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    public Usage getInitUsage() {
        return new UAnd(new Usage[]{CREATE_PROXY, new UFile("CertDir")});
    }

    private static final int OID_OLD = 2;           // default
    private static final int OID_GLOBUS = 3;
    private static final int OID_RFC820 = 4;
    private static final int DELEGATION_NONE = 1;
    private static final int DELEGATION_LIMITED = 2;
    private static final int DELEGATION_FULL = 3;   // default
    public SecurityAdaptor initAndCreateSecurityAdaptor(Map attributes) throws Exception {
        // required attributes
        System.setProperty("X509_USER_CERT", (String) attributes.get("UserCert"));
        System.setProperty("X509_USER_KEY", (String) attributes.get("UserKey"));
        System.setProperty("X509_CERT_DIR", (String) attributes.get("CertDir"));
        System.setProperty("VOMSDIR", (String) attributes.get("VomsDir"));
        URI uri = new URI((String) attributes.get("Server"));
        if (uri.getHost()==null) {
            throw new BadParameter("Attribute Server has no host name: "+uri.toString());
        }
        VOMSServerInfo server = new VOMSServerInfo();
        server.setHostName(uri.getHost());
        server.setPort(uri.getPort());
        server.setHostDn(uri.getPath());
        server.setVoName((String) attributes.get("UserVO"));
        VOMSProxyInit proxyInit = !"".equals(attributes.get("UserPass"))
                ? VOMSProxyInit.instance((String) attributes.get("UserPass"))
                : VOMSProxyInit.instance();
        proxyInit.addVomsServer(server);
        proxyInit.setProxyOutputFile((String) attributes.get("UserProxy"));
        VOMSRequestOptions o = new VOMSRequestOptions();
        o.setVoName((String) attributes.get("UserVO"));

        // optional attributes
        if (attributes.containsKey("UserFQAN")) {
            o.addFQAN((String) attributes.get("UserFQAN"));
        }
        if (attributes.containsKey("LifeTime")) {
            int lifetime = UDuration.toInt(attributes.get("LifeTime"));
            proxyInit.setProxyLifetime(lifetime);
        }
        if (attributes.containsKey("Delegation")) {
            if (((String)attributes.get("Delegation")).equalsIgnoreCase("none")) {
                proxyInit.setDelegationType(DELEGATION_NONE);
            } else if (((String)attributes.get("Delegation")).equalsIgnoreCase("limited")) {
                proxyInit.setDelegationType(DELEGATION_LIMITED);
            } else if (((String)attributes.get("Delegation")).equalsIgnoreCase("full")) {
                proxyInit.setDelegationType(DELEGATION_FULL);
            }
        }
        if (attributes.containsKey("ProxyType")) {
            if (((String)attributes.get("ProxyType")).equalsIgnoreCase("old")) {
                proxyInit.setProxyType(OID_OLD);
            } else if (((String)attributes.get("ProxyType")).equalsIgnoreCase("globus")) {
                proxyInit.setProxyType(OID_GLOBUS);
            } else if (((String)attributes.get("ProxyType")).equalsIgnoreCase("RFC820")) {
                proxyInit.setProxyType(OID_RFC820);
            }
        }

        // create
        ArrayList options = new ArrayList();
        options.add(o);
        GlobusCredential cred = proxyInit.getVomsProxy(options);
        return new VOMSSecurityAdaptor(cred);
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
                GSSCredential.ACCEPT_ONLY);
    }
}
