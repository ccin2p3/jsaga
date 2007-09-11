package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyFile;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyObject;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.error.BadParameter;
import org.ogf.saga.error.IncorrectState;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityAdaptorBuilder
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityAdaptorBuilder implements InitializableSecurityAdaptorBuilder {
    private static final int MIN_LIFETIME_FOR_USING = 3*3600;   // 3 hours
//    private static final int MIN_LIFETIME_FOR_RENEW = 30*60;    // 30 minutes
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    private static final Usage LOCAL_PROXY_OBJECT = new UProxyObject("UserProxyObject", MIN_LIFETIME_FOR_USING);
    private static final Usage LOCAL_PROXY_FILE = new UProxyFile("UserProxy", MIN_LIFETIME_FOR_USING);
    private static final Usage RENEW_PROXY_OBJECT_WITH_PASS = new UAnd(new Usage[]{
            new UNoPrompt("UserProxyObject"), new U("UserName"), new UHidden("MyProxyPass")});
    private static final Usage RENEW_PROXY_FILE_WITH_PASS = new UAnd(new Usage[]{
            new U("UserProxy"), new U("UserName"), new UHidden("MyProxyPass")});
/*
    private static final Usage RENEW_PROXY_OBJECT_WITH_PROXY = new UProxyObject("UserProxyObject", MIN_LIFETIME_FOR_RENEW);
    private static final Usage RENEW_PROXY_FILE_WITH_PROXY = new UProxyFile("UserProxy", MIN_LIFETIME_FOR_RENEW);
*/
    private static final Usage CREATE_PROXY = new UAnd(new Usage[]{
            new U("UserProxy"), new UFile("UserCert"), new UFile("UserKey"), new UHidden("UserPass"),
            new U("Server"),
            new U("UserName"),
            new UHidden("MyProxyPass"),
/*
            new UOptional("UserName"),
            new UHidden("MyProxyPass") {
                public String toString() {return "[*"+m_name+"*]";}
                protected void throwExceptionIfInvalid(Object value) throws Exception {}
            },
*/
            new UDuration("LifeTime") {
                protected Object throwExceptionIfInvalid(Object value) throws Exception {
                    return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                }
            },
    });

    public String getType() {
        return "MyProxy";
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        // local proxy
                        LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE,
                        // get proxy from server
                        RENEW_PROXY_OBJECT_WITH_PASS, RENEW_PROXY_FILE_WITH_PASS,
//                        RENEW_PROXY_OBJECT_WITH_PROXY, RENEW_PROXY_FILE_WITH_PROXY,
                        // create proxy
                        CREATE_PROXY}),
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
                new Default("Server", env.getProperty("MYPROXY_SERVER")),
                new Default("UserName", System.getProperty("user.name")),
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        if (LOCAL_PROXY_OBJECT.getMissingValues(attributes) == null) {
            return new MyProxySecurityAdaptor((GSSCredential) attributes.get("UserProxyObject"));
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            return new MyProxySecurityAdaptor(load(new File((String) attributes.get("UserProxy"))));
        } else if (RENEW_PROXY_OBJECT_WITH_PASS.getMissingValues(attributes) == null) {
            return new MyProxySecurityAdaptor(renewCredential(null, attributes));
        } else if (RENEW_PROXY_FILE_WITH_PASS.getMissingValues(attributes) == null) {
            GSSCredential cred = renewCredential(null, attributes);
            save(new File((String) attributes.get("UserProxy")), cred);
            return new MyProxySecurityAdaptor(cred);
/*
        } else if (RENEW_PROXY_OBJECT_WITH_PROXY.getMissingValues(attributes) == null) {
            GSSCredential oldCred = (GSSCredential) attributes.get("UserProxyObject");
            return new MyProxySecurityAdaptor(renewCredential(oldCred, attributes));
        } else if (RENEW_PROXY_FILE_WITH_PROXY.getMissingValues(attributes) == null) {
            GSSCredential oldCred = load(new File((String) attributes.get("UserProxy")));
            GSSCredential cred = renewCredential(oldCred, attributes);
            save(new File((String) attributes.get("UserProxy")), cred);
            return new MyProxySecurityAdaptor(cred);
*/
        } else if (CREATE_PROXY.getMissingValues(attributes) == null) {
            GSSCredential cred = new GlobusProxyFactory(attributes).createProxy();
            String userName = (String) attributes.get("UserName");
            String myProxyPass = (String) attributes.get("MyProxyPass");
            int lifetime = attributes.containsKey("LifeTime")
                    ? UDuration.toInt(attributes.get("LifeTime"))
                    : DEFAULT_DELEGATED_PROXY_LIFETIME;  // default lifetime for delegated proxies
            createMyProxy(attributes).put(cred, userName, myProxyPass, lifetime);
            return new MyProxySecurityAdaptor(cred);
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    public Usage getInitUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        // local proxy
                        CREATE_PROXY,
                        // get proxy from server
                        RENEW_PROXY_FILE_WITH_PASS
//                        ,RENEW_PROXY_FILE_WITH_PROXY
                }),
                new UFile("CertDir")});
    }

    public SecurityAdaptor initAndCreateSecurityAdaptor(Map attributes) throws Exception {
        if (CREATE_PROXY.getMissingValues(attributes) == null) {
            GSSCredential cred = new GlobusProxyFactory(attributes).createProxy();
            String userName = (String) attributes.get("UserName");
            String myProxyPass = (String) attributes.get("MyProxyPass");
            int lifetime = attributes.containsKey("LifeTime")
                    ? UDuration.toInt(attributes.get("LifeTime"))
                    : DEFAULT_DELEGATED_PROXY_LIFETIME;  // default lifetime for delegated proxies
            createMyProxy(attributes).put(cred, userName, myProxyPass, lifetime);
            return new MyProxySecurityAdaptor(cred);
        } else if (RENEW_PROXY_FILE_WITH_PASS.getMissingValues(attributes) == null) {
            GSSCredential cred = renewCredential(null, attributes);
            save(new File((String) attributes.get("UserProxy")), cred);
            return new MyProxySecurityAdaptor(cred);
/*
        } else if (RENEW_PROXY_FILE_WITH_PROXY.getMissingValues(attributes) == null) {
            GSSCredential oldCred = load(new File((String) attributes.get("UserProxy")));
            GSSCredential cred = renewCredential(oldCred, attributes);
            save(new File((String) attributes.get("UserProxy")), cred);
            return new MyProxySecurityAdaptor(cred);
*/
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
    }

    private static GSSCredential renewCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException {
        String userName = (String) attributes.get("UserName");
        String myProxyPass = (String) attributes.get("MyProxyPass");
        int lifetime = attributes.containsKey("LifeTime")
                ? UDuration.toInt(attributes.get("LifeTime"))
                : DEFAULT_DELEGATED_PROXY_LIFETIME;  // effective lifetime for delegated proxy
        return createMyProxy(attributes).get(oldCred, userName, myProxyPass, lifetime);
    }

    private static MyProxy createMyProxy(Map attributes) throws URISyntaxException {
        String[] server = ((String) attributes.get("Server")).split(":");
        String host = server[0];
        int port = (server.length>1 ? Integer.parseInt(server[1]) : MyProxy.DEFAULT_PORT);
        MyProxy myProxy = new MyProxy(host, port);
/*
        String subjectDN = null;
        if (subjectDN != null) {
            myProxy.setAuthorization(new IdentityAuthorization(subjectDN));
        }
*/
        return myProxy;
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

    private static void save(File proxyFile, GSSCredential cred) throws GSSException, IOException {
        byte[] proxyBytes = ((ExtendedGSSCredential) cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(proxyFile);
        out.write(proxyBytes);
        out.close();
    }
}
