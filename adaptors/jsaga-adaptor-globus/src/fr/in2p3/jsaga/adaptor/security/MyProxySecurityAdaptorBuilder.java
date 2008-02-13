package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyFile;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyObject;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectState;
import org.ogf.saga.error.NoSuccess;

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
public class MyProxySecurityAdaptorBuilder implements ExpirableSecurityAdaptorBuilder {
//    private static final int USAGE_INIT_PKCS12 = 1;
    private static final int USAGE_INIT_PEM = 2;
    private static final int USAGE_LOCAL_MEMORY = 3;
    private static final int USAGE_LOCAL_LOAD = 4;
    private static final int USAGE_RENEW_MEMORY_WITH_PASSPHRASE = 5;
    private static final int USAGE_RENEW_LOAD_WITH_PASSPHRASE = 6;
//    private static final int USAGE_RENEW_MEMORY_WITH_PROXY = 7;
//    private static final int USAGE_RENEW_LOAD_WITH_PROXY = 8;

    private static final int MIN_LIFETIME_FOR_USING = 3*3600;   // 3 hours
//    private static final int MIN_LIFETIME_FOR_RENEW = 30*60;    // 30 minutes
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*12*3600;
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    public String getType() {
        return "MyProxy";
    }

    public Class getSecurityAdaptorClass() {
        return MyProxySecurityAdaptor.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        new UAnd(USAGE_INIT_PEM, new Usage[]{
                                new UFile(Context.USERCERT), new UFile(Context.USERKEY),
                                new UFilePath(Context.USERPROXY), new UHidden(Context.USERPASS),
                                new U(Context.SERVER),
                                new U(Context.USERID),
                                new UHidden(GlobusContext.MYPROXYPASS),
/*
                                new UOptional(Context.USERID),
                                new UHidden(GlobusContext.MYPROXYPASS) {
                                    public String toString() {return "[*"+m_name+"*]";}
                                    protected void throwExceptionIfInvalid(Object value) throws Exception {}
                                },
*/
                                new UDuration(Context.LIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                },
                        }),

                        // local proxy
                        new UProxyObject(USAGE_LOCAL_MEMORY, GlobusContext.USERPROXYOBJECT, MIN_LIFETIME_FOR_USING),
                        new UProxyFile(USAGE_LOCAL_LOAD, Context.USERPROXY, MIN_LIFETIME_FOR_USING),

                        // get proxy from server with passphrase
                        new UAnd(USAGE_RENEW_MEMORY_WITH_PASSPHRASE, new Usage[]{
                                new UNoPrompt(GlobusContext.USERPROXYOBJECT), new U(Context.USERID), new UHidden(GlobusContext.MYPROXYPASS)}),
                        new UAnd(USAGE_RENEW_LOAD_WITH_PASSPHRASE, new Usage[]{
                                new U(Context.USERPROXY), new U(Context.USERID), new UHidden(GlobusContext.MYPROXYPASS)})

                        // get proxy from server with old proxy
/*
                        new UProxyObject(USAGE_RENEW_MEMORY_WITH_PROXY, GlobusContext.USERPROXYOBJECT, MIN_LIFETIME_FOR_RENEW),
                        new UProxyFile(USAGE_RENEW_LOAD_WITH_PROXY, Context.USERPROXY, MIN_LIFETIME_FOR_RENEW)
*/
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

    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectState, NoSuccess {
        try {
            switch(usage) {
                case USAGE_INIT_PEM:
                {
                    // create proxy
                    GSSCredential cred = new GlobusProxyFactory(attributes, GlobusProxyFactory.OID_OLD, GlobusProxyFactory.CERTIFICATE_PEM).createProxy();
                    // send it to MyProxy server
                    String userId = (String) attributes.get(Context.USERID);
                    String myProxyPass = (String) attributes.get(GlobusContext.MYPROXYPASS);
                    int storedLifetime = attributes.containsKey(Context.LIFETIME)
                            ? UDuration.toInt(attributes.get(Context.LIFETIME))
                            : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
                    createMyProxy(attributes).put(cred, userId, myProxyPass, storedLifetime);
                    // returns
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_LOCAL_MEMORY:
                {
                    GSSCredential cred = InMemoryProxySecurityAdaptor.toGSSCredential((String) attributes.get(GlobusContext.USERPROXYOBJECT));
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_LOCAL_LOAD:
                {
                    GSSCredential cred = load(new File((String) attributes.get(Context.USERPROXY)));
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_RENEW_MEMORY_WITH_PASSPHRASE:
                {
                    GSSCredential cred = renewCredential(null, attributes);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_RENEW_LOAD_WITH_PASSPHRASE:
                {
                    GSSCredential cred = renewCredential(null, attributes);
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
                    return this.createSecurityAdaptor(cred, attributes);
                }
/*
                case USAGE_RENEW_MEMORY_WITH_PROXY:
                {
                    GSSCredential oldCred = (GSSCredential) attributes.get(GlobusContext.USERPROXYOBJECT);
                    GSSCredential cred = renewCredential(oldCred, attributes);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_RENEW_LOAD_WITH_PROXY:
                {
                    GSSCredential oldCred = load(new File((String) attributes.get(Context.USERPROXY)));
                    GSSCredential cred = renewCredential(oldCred, attributes);
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
                    return this.createSecurityAdaptor(cred, attributes);
                }
*/
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
    private SecurityAdaptor createSecurityAdaptor(GSSCredential cred, Map attributes) throws IncorrectState {
        String userId = (String) attributes.get(Context.USERID);
        String myProxyPass = (String) attributes.get(GlobusContext.MYPROXYPASS);
        return new MyProxySecurityAdaptor(cred, userId, myProxyPass);
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        String proxyFile = (String) attributes.get(Context.USERPROXY);
        Util.destroy(proxyFile);
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
