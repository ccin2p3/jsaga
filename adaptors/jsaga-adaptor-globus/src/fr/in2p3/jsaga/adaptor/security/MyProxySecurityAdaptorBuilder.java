package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
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
import org.ogf.saga.context.Context;

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
public class MyProxySecurityAdaptorBuilder implements SecurityAdaptorBuilder {
    private static final int MIN_LIFETIME_FOR_USING = 3*3600;   // 3 hours
//    private static final int MIN_LIFETIME_FOR_RENEW = 30*60;    // 30 minutes
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    private static final Usage LOCAL_PROXY_OBJECT = new UProxyObject(GlobusContext.USERPROXYOBJECT, MIN_LIFETIME_FOR_USING);
    private static final Usage LOCAL_PROXY_FILE = new UProxyFile(Context.USERPROXY, MIN_LIFETIME_FOR_USING);
    private static final Usage RENEW_PROXY_OBJECT_WITH_PASS = new UAnd(new Usage[]{
            new UNoPrompt(GlobusContext.USERPROXYOBJECT), new U(Context.USERID), new UHidden(GlobusContext.MYPROXYPASS)});
    private static final Usage RENEW_PROXY_FILE_WITH_PASS = new UAnd(new Usage[]{
            new U(Context.USERPROXY), new U(Context.USERID), new UHidden(GlobusContext.MYPROXYPASS)});
/*
    private static final Usage RENEW_PROXY_OBJECT_WITH_PROXY = new UProxyObject(GlobusContext.USERPROXYOBJECT, MIN_LIFETIME_FOR_RENEW);
    private static final Usage RENEW_PROXY_FILE_WITH_PROXY = new UProxyFile(Context.USERPROXY, MIN_LIFETIME_FOR_RENEW);
*/

    public String getType() {
        return "MyProxy";
    }

    public Class getSecurityAdaptorClass() {
        return MyProxySecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        // local proxy
                        LOCAL_PROXY_OBJECT, LOCAL_PROXY_FILE,
                        // get proxy from server
                        RENEW_PROXY_OBJECT_WITH_PASS, RENEW_PROXY_FILE_WITH_PASS,
//                        RENEW_PROXY_OBJECT_WITH_PROXY, RENEW_PROXY_FILE_WITH_PROXY,
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
                                                : GlobusSecurityAdaptorBuilder.getUnixUID()
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
                new Default(Context.SERVER, env.getProperty("MYPROXY_SERVER")),
                new Default(Context.USERID, System.getProperty("user.name")),
        };
    }

    public SecurityAdaptor createSecurityAdaptor(Map attributes) throws Exception {
        GSSCredential cred;
        if (LOCAL_PROXY_OBJECT.getMissingValues(attributes) == null) {
            cred = InMemoryProxySecurityAdaptor.toGSSCredential((String) attributes.get(GlobusContext.USERPROXYOBJECT));
        } else if (LOCAL_PROXY_FILE.getMissingValues(attributes) == null) {
            cred = load(new File((String) attributes.get(Context.USERPROXY)));
        } else if (RENEW_PROXY_OBJECT_WITH_PASS.getMissingValues(attributes) == null) {
            cred = renewCredential(null, attributes);
        } else if (RENEW_PROXY_FILE_WITH_PASS.getMissingValues(attributes) == null) {
            cred = renewCredential(null, attributes);
            save(new File((String) attributes.get(Context.USERPROXY)), cred);
/*
        } else if (RENEW_PROXY_OBJECT_WITH_PROXY.getMissingValues(attributes) == null) {
            GSSCredential oldCred = (GSSCredential) attributes.get(GlobusContext.USERPROXYOBJECT);
            cred = renewCredential(oldCred, attributes);
        } else if (RENEW_PROXY_FILE_WITH_PROXY.getMissingValues(attributes) == null) {
            GSSCredential oldCred = load(new File((String) attributes.get(Context.USERPROXY)));
            cred = renewCredential(oldCred, attributes);
            save(new File((String) attributes.get(Context.USERPROXY)), cred);
*/
        } else {
            throw new BadParameter("Missing attribute(s): "+this.getUsage().getMissingValues(attributes));
        }
        String userId = (String) attributes.get(Context.USERID);
        String myProxyPass = (String) attributes.get(GlobusContext.MYPROXYPASS);
        return new MyProxySecurityAdaptor(cred, userId, myProxyPass);
    }

    private static GSSCredential renewCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException {
        String userId = (String) attributes.get(Context.USERID);
        String myProxyPass = (String) attributes.get(GlobusContext.MYPROXYPASS);
        int delegatedLifetime = attributes.containsKey(Context.LIFETIME)
                ? UDuration.toInt(attributes.get(Context.LIFETIME))
                : DEFAULT_DELEGATED_PROXY_LIFETIME;  // effective lifetime for delegated proxy
        return createMyProxy(attributes).get(oldCred, userId, myProxyPass, delegatedLifetime);
    }

    protected static MyProxy createMyProxy(Map attributes) throws URISyntaxException {
        String[] server = ((String) attributes.get(Context.SERVER)).split(":");
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
