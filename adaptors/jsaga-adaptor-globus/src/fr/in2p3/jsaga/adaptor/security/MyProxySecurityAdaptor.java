package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.defaults.EnvironmentVariables;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyFile;
import fr.in2p3.jsaga.adaptor.security.usage.UProxyObject;
import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.myproxy.InitParams;
import org.globus.myproxy.InfoParams;
import org.globus.myproxy.DestroyParams;
import org.globus.myproxy.GetParams;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityAdaptor extends GlobusSecurityAdaptor {
    public static final int USAGE_GET_DELEGATED_MEMORY = 20;
    public static final int USAGE_GET_DELEGATED_LOAD = 21;

//    private static final int MIN_LIFETIME_FOR_USING = 3*3600;   // 3 hours
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*24*3600;
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    @Override
    public String getType() {
        return "MyProxy";
    }

    @Override
    public Class getSecurityCredentialClass() {
        return MyProxySecurityCredential.class;
    }

    @Override
    public Usage getUsage() {
        return new UAnd.Builder()
                .and(new UOr.Builder()
                        // get delegated proxy from server
                        .or(new UAnd.Builder()
                                    .id(USAGE_GET_DELEGATED_MEMORY)
                                    .and(new UNoPrompt(GlobusContext.USERPROXYOBJECT))
                                    .and(new UDuration(GlobusContext.DELEGATIONLIFETIME))
                                    .build()
                        )
                        .or(new UAnd.Builder()
                                    .id(USAGE_GET_DELEGATED_LOAD)
                                    .and(new UFile(Context.USERPROXY))
                                    .and(new UDuration(GlobusContext.DELEGATIONLIFETIME))
                                    .build()
                        )
                        // local proxy
                        .or(new UNoPrompt(USAGE_MEMORY, GlobusContext.USERPROXYOBJECT/*, MIN_LIFETIME_FOR_USING*/))
                        .or(new UFile(USAGE_LOAD, Context.USERPROXY/*, MIN_LIFETIME_FOR_USING*/))

                        // create and store proxy
                        .or(getPKCS12orPEM())
                        .build()
                )
                .and(new U(Context.SERVER))
                .and(new UOptional(Context.USERID))
                .and(new UOptional(GlobusContext.MYPROXYPASS))
                .and(new UFile(Context.CERTREPOSITORY))
                .build();
    }

    @Override
    public Default[] getDefaults(Map map) throws IncorrectStateException {
        EnvironmentVariables env = EnvironmentVariables.getInstance();
//        return new Default[]{
//                // concat with ".myproxy" to avoid conflict with Globus context type
//                new Default(Context.USERPROXY, new String[]{
//                        env.getProperty("X509_USER_PROXY")!=null ? env.getProperty("X509_USER_PROXY")+".myproxy" : null,
//                        System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"x509up_u"+
//                                (System.getProperty("os.name").toLowerCase().startsWith("windows")
//                                        ? "_"+System.getProperty("user.name").toLowerCase()
//                                        : (env.getProperty("UID")!=null
//                                                ? env.getProperty("UID")
//                                                : getUnixUID()
//                                          )
//                                )+".myproxy"}),
//                new Default(Context.USERCERT, new File[]{
//                        new File(env.getProperty("X509_USER_CERT")+""),
//                        new File(System.getProperty("user.home")+"/.globus/usercert.pem")}),
//                new Default(Context.USERKEY, new File[]{
//                        new File(env.getProperty("X509_USER_KEY")+""),
//                        new File(System.getProperty("user.home")+"/.globus/userkey.pem")}),
//                new Default(Context.CERTREPOSITORY, new File[]{
//                        new File(env.getProperty("X509_CERT_DIR")+""),
//                        new File(System.getProperty("user.home")+"/.globus/certificates/"),
//                        new File("/etc/grid-security/certificates/")}),
//                new Default(Context.SERVER, env.getProperty("MYPROXY_SERVER")),
//        };
        Default[] parentDefault = super.getDefaults(map);
        Default[] thisDefault = new Default[parentDefault.length+1];
        System.arraycopy(parentDefault, 0, thisDefault, 0, parentDefault.length);
        thisDefault[parentDefault.length] = new Default(Context.SERVER, env.getProperty("MYPROXY_SERVER"));
        return thisDefault;
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        try {
            switch(usage) {
                case USAGE_INIT_PEM:
                {
                    // build proxy
//                    GSSCredential cred = new GlobusProxyFactory(attributes, GlobusProxyFactory.OID_OLD, GlobusProxyFactory.CERTIFICATE_PEM).createProxy();
                    GSSCredential cred = ((GlobusSecurityCredential)super.createSecurityCredential(usage, attributes, contextId)).getGSSCredential();

                    InitParams proxyParameters = new InitParams();

                    // send it to MyProxy server
                    String userId = getUserName(cred, attributes);
                    proxyParameters.setUserName(userId);

                    if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
                    	proxyParameters.setPassphrase((String)attributes.get(GlobusContext.MYPROXYPASS));
                    }
                    
                    int storedLifetime = attributes.containsKey(Context.LIFETIME)
                            ? UDuration.toInt(attributes.get(Context.LIFETIME))
                            : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
                    proxyParameters.setLifetime(storedLifetime);

                    MyProxy server = getServer(attributes);
                    server.put(cred, proxyParameters);

                    // destroy local temporary proxy (requires anonymous to be authorized by server's default trusted_retrievers policy)
                    //Util.destroy(tempFile);

                    // returns
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_MEMORY:
//                {
//                    GSSCredential cred = InMemoryProxySecurityCredential.toGSSCredential((String) attributes.get(GlobusContext.USERPROXYOBJECT));
//                    return this.createSecurityAdaptor(cred, attributes);
//                }
                case USAGE_LOAD:
                {
//                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
//                    GSSCredential cred = load(new File((String) attributes.get(Context.USERPROXY)));
//                    return this.createSecurityAdaptor(cred, attributes);
                    return super.createSecurityCredential(usage, attributes, contextId);
                }
                case USAGE_GET_DELEGATED_MEMORY:
                {
                    // get old proxy (required unless anonymous is authorized by server's default trusted_retrievers policy)
                    GSSCredential oldCred = InMemoryProxySecurityCredential.toGSSCredential((String) attributes.get(GlobusContext.USERPROXYOBJECT));

                    // get delegated proxy
                    GSSCredential cred = getDelegatedCredential(oldCred, attributes);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_GET_DELEGATED_LOAD:
                {
                    // get old proxy (required unless anonymous is authorized by server's default trusted_retrievers policy)
                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
                    GSSCredential oldCred = load(new File((String) attributes.get(Context.USERPROXY)));

                    // get delegated proxy
                    GSSCredential cred = getDelegatedCredential(oldCred, attributes);
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
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
        File certRepository = new File((String) attributes.get(Context.CERTREPOSITORY));
        String server = (String) attributes.get(Context.SERVER);

        InfoParams proxyParameters = new InfoParams();
        String userId = getUserName(cred, attributes);
        proxyParameters.setUserName(userId);

        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(GlobusContext.MYPROXYPASS));
        }

        return new MyProxySecurityCredential(cred, certRepository, server, proxyParameters);
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        // get attributes
        File proxy = new File((String) attributes.get(Context.USERPROXY));
        if (!proxy.exists()) {
            return;
        }
        GSSCredential cred = load(proxy);
        DestroyParams proxyParameters = new DestroyParams();

        String userId = getUserName(cred, attributes);
        proxyParameters.setUserName(userId);
        
        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(GlobusContext.MYPROXYPASS));
        }
        
        // destroy remote proxy
        MyProxy server = getServer(attributes);
        server.destroy(cred, proxyParameters);
        
        // destroy local proxy
//        Util.destroy(proxy);
        super.destroySecurityAdaptor(attributes, contextId);
    }

    private static GSSCredential getDelegatedCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException, GSSException {
        GetParams proxyParameters = new GetParams();

        String userId = getUserName(oldCred, attributes);
        proxyParameters.setUserName(userId);

        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(GlobusContext.MYPROXYPASS));
        }

        int delegatedLifetime = attributes.containsKey(GlobusContext.DELEGATIONLIFETIME)
        		? UDuration.toInt(attributes.get(GlobusContext.DELEGATIONLIFETIME))
                : DEFAULT_DELEGATED_PROXY_LIFETIME;  // effective lifetime for delegated proxy
        proxyParameters.setLifetime(delegatedLifetime);
        
        MyProxy server = getServer(attributes);

        return server.get(oldCred, proxyParameters);
    }

    private static MyProxy getServer(Map attributes) throws URISyntaxException {
        String server = (String) attributes.get(Context.SERVER);
        return getServer(server);
    }
    static MyProxy getServer(String server) {
        String[] array = (server).split(":");
        String host = array[0];
        int port = (array.length>1 ? Integer.parseInt(array[1]) : MyProxy.DEFAULT_PORT);
        MyProxy myProxy = new MyProxy(host, port);
/*
        String subjectDN = null;
        if (subjectDN != null) {
            myProxy.setAuthorization(new IdentityAuthorization(subjectDN));
        }
*/
        return myProxy;
    }

    private static void save(File proxyFile, GSSCredential cred) throws GSSException, IOException {
        byte[] proxyBytes = ((ExtendedGSSCredential) cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(proxyFile);
        out.write(proxyBytes);
        out.close();
    }
    
    private static String getUserName(GSSCredential cred, Map attributes) {
        return attributes.get(Context.USERID) != null 
        			? (String) attributes.get(Context.USERID) 
        			: ((GlobusGSSCredentialImpl)cred).getX509Credential().getIdentity();
    }
}
