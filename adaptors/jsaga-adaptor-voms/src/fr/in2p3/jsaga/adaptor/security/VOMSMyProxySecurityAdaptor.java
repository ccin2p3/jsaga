package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.InMemoryProxySecurityCredential;
import org.globus.common.CoGProperties;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.globus.myproxy.InitParams;
import org.globus.myproxy.DestroyParams;
import org.globus.myproxy.GetParams;
import org.globus.util.Util;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/*
 * ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) *** *** http://cc.in2p3.fr/
 * *** *************************************************** File:
 * VOMSMyProxySecurityAdaptor Author: Sylvain Reynaud (sreynaud@in2p3.fr) Date:
 * 26 janv. 2009 ***************************************************
 * Description:
 */
/**
 *
 */
public class VOMSMyProxySecurityAdaptor extends VOMSSecurityAdaptor implements ExpirableSecurityAdaptor {

    public static final int USAGE_GET_DELEGATED_MEMORY = 20; // 1 to 19 reserved by super class
    public static final int USAGE_GET_DELEGATED_LOAD = 21;
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7 * 24 * 3600;
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12 * 3600;

    public String getType() {
        return "VOMSMyProxy";
    }

    public Class getSecurityCredentialClass() {
        return VOMSMyProxySecurityCredential.class;
    }

    public Usage getUsage() {
        return new UAnd.Builder()
            .and(new UOr.Builder()
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
                    .or(new UNoPrompt(GlobusSecurityAdaptor.USAGE_MEMORY, GlobusContext.USERPROXYOBJECT))
                    .or(new UProxyValue(GlobusSecurityAdaptor.USAGE_LOAD,  VOMSContext.USERPROXYSTRING))
                    .or(new UFile(GlobusSecurityAdaptor.USAGE_LOAD, Context.USERPROXY))
                    .or(new UAnd.Builder()
                            .and(new UFile(USAGE_INIT_PROXY, VOMSContext.INITIALPROXY))
                            .and(getInitProxyUsages())
                            // TODO: why not UDuration?
                            .and(new UOptional(GlobusContext.DELEGATIONLIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value != null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                }
                            )
                            .build()
                       )
                    .or(new UAnd.Builder()
                            .and(fr.in2p3.jsaga.adaptor.security.usage.Util.buildCertsUsage())
                            .and(new UHidden(Context.USERPASS))
                            .and(getInitProxyUsages())
                            // TODO: why not UDuration?
                            .and(new UOptional(GlobusContext.DELEGATIONLIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value != null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                }
                            )
                            .build()
                       )
                    .build()
                )
            .and(new UFile(Context.CERTREPOSITORY))
            .and(new U(VOMSContext.MYPROXYSERVER))
            .and(new UOptional(VOMSContext.MYPROXYUSERID))
            .and(new UOptional(GlobusContext.MYPROXYPASS))
            .build();
    }
    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            switch (usage) {
                case GlobusSecurityAdaptor.USAGE_INIT_PKCS12:
                case GlobusSecurityAdaptor.USAGE_INIT_PEM: 
                case USAGE_INIT_PROXY: {
                    VOMSSecurityCredential adaptor = (VOMSSecurityCredential) super.createSecurityCredential(usage, attributeForVOMS(attributes), contextId);

                    GSSCredential cred = adaptor.getGSSCredential();

                    // send it to MyProxy server
                    storeCredential(cred, attributes);

                    // destroy local temporary proxy (requires anonymous to be authorized by server's default trusted_retrievers policy)
                    //Util.destroy(tempFile);

                    // returns
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case GlobusSecurityAdaptor.USAGE_MEMORY:
                case GlobusSecurityAdaptor.USAGE_LOAD: {
                    // creates a VOMSMyProxySecurityCredential
                    return super.createSecurityCredential(usage, attributes, contextId);
                }
                case USAGE_GET_DELEGATED_MEMORY: {
                    // get old proxy (required unless anonymous is authorized by server's default trusted_retrievers policy)
                    GSSCredential oldCred = InMemoryProxySecurityCredential.toGSSCredential((String) attributes.get(GlobusContext.USERPROXYOBJECT));

                    // get delegated proxy
                    GSSCredential cred = getDelegatedCredential(oldCred, attributes);
//                    GSSCredential resignedCred = new VOMSProxyFactory(attributes, cred).createProxy();
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_GET_DELEGATED_LOAD: {
                    // get old proxy (required unless anonymous is authorized by server's default trusted_retrievers policy)
                    CoGProperties.getDefault().setCaCertLocations((String) attributes.get(Context.CERTREPOSITORY));
                    GSSCredential oldCred = load(new File((String) attributes.get(Context.USERPROXY)));

                    // get delegated proxy
                    GSSCredential cred = getDelegatedCredential(oldCred, attributes);
//                    GSSCredential resignedCred = new VOMSProxyFactory(attributes, cred).createProxy();
                    save(new File((String) attributes.get(Context.USERPROXY)), cred);
                    return this.createSecurityAdaptor(cred, attributes);
                }
                default:
                    throw new NoSuccessException("INTERNAL ERROR: unexpected exception");
            }
        } catch (IncorrectStateException e) {
            throw e;
        } catch (NoSuccessException e) {
            throw e;
        } catch (Exception e) {
            throw new NoSuccessException(e);
        }
    }

    private Map<String, String> attributeForVOMS(Map<String, String> attributes) {
        Map<String, String> attributeForVOMS = new HashMap<String, String>();
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            attributeForVOMS.put(e.getKey(), e.getValue());
        }

        String lifeTime = attributes.get(GlobusContext.DELEGATIONLIFETIME);
        if(lifeTime != null) attributeForVOMS.put(Context.LIFETIME, lifeTime);

        return attributeForVOMS;
    }

    protected SecurityCredential createSecurityAdaptor(GSSCredential cred, Map attributes) {
        return new VOMSMyProxySecurityCredential(cred, attributes);
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        // get attributes
        File proxy = new File((String) attributes.get(Context.USERPROXY));
        if (!proxy.exists()) {
            return;
        }
        GSSCredential cred = load(proxy);
        DestroyParams proxyParameters = new DestroyParams();

        String myProxyServer = (String) attributes.get(VOMSContext.MYPROXYSERVER);

        String myProxyUserId = getUserName(cred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
            proxyParameters.setPassphrase((String) attributes.get(GlobusContext.MYPROXYPASS));
        }

        // destroy remote proxy
        MyProxy server = getServer(myProxyServer);
        server.destroy(cred, proxyParameters);

        // destroy local proxy
        Util.destroy((String) attributes.get(Context.USERPROXY));
    }

    private static MyProxy getServer(String server) {
        String[] array = (server).split(":");
        String host = array[0];
        int port = (array.length > 1 ? Integer.parseInt(array[1]) : MyProxy.DEFAULT_PORT);
        MyProxy myProxy = new MyProxy(host, port);
        /*
         * String subjectDN = null; if (subjectDN != null) {
         * myProxy.setAuthorization(new IdentityAuthorization(subjectDN)); }
         */
        return myProxy;
    }

    private static void storeCredential(GSSCredential newCred, Map attributes) throws ParseException, MyProxyException {
        InitParams proxyParameters = new InitParams();

        String myProxyUserId = getUserName(newCred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
            proxyParameters.setPassphrase((String) attributes.get(GlobusContext.MYPROXYPASS));
        }

        int storedLifetime = attributes.containsKey(GlobusContext.DELEGATIONLIFETIME)
                ? UDuration.toInt(attributes.get(GlobusContext.DELEGATIONLIFETIME))
                : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
        proxyParameters.setLifetime(storedLifetime);

        MyProxy server = getServer((String) attributes.get(VOMSContext.MYPROXYSERVER));
        server.put(newCred, proxyParameters);

    }

    private static GSSCredential getDelegatedCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException {
        GetParams proxyParameters = new GetParams();

        String myProxyUserId = getUserName(oldCred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(GlobusContext.MYPROXYPASS) != null) {
            proxyParameters.setPassphrase((String) attributes.get(GlobusContext.MYPROXYPASS));
        }

        int delegatedLifetime = attributes.containsKey(GlobusContext.DELEGATIONLIFETIME)
                ? UDuration.toInt(attributes.get(GlobusContext.DELEGATIONLIFETIME))
                : DEFAULT_DELEGATED_PROXY_LIFETIME;  // effective lifetime for delegated proxy
        proxyParameters.setLifetime(delegatedLifetime);

        MyProxy server = getServer((String) attributes.get(VOMSContext.MYPROXYSERVER));
        return server.get(oldCred, proxyParameters);
    }

    private static void save(File proxyFile, GSSCredential cred) throws GSSException, IOException {
        byte[] proxyBytes = ((ExtendedGSSCredential) cred).export(ExtendedGSSCredential.IMPEXP_OPAQUE);
        FileOutputStream out = new FileOutputStream(proxyFile);
        out.write(proxyBytes);
        out.close();
    }

    private static String getUserName(GSSCredential cred, Map attributes) {
        return attributes.get(VOMSContext.MYPROXYUSERID) != null
                ? (String) attributes.get(VOMSContext.MYPROXYUSERID)
                : ((GlobusGSSCredentialImpl) cred).getX509Credential().getIdentity();
    }
}
