package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
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
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxySecurityAdaptor extends VOMSSecurityAdaptor implements ExpirableSecurityAdaptor {
    private static final int USAGE_GET_DELEGATED_MEMORY = 5; // 1 to 4 reserved by super class
    private static final int USAGE_GET_DELEGATED_LOAD = 6;
    private static final int DEFAULT_STORED_PROXY_LIFETIME = 7*24*3600;
    private static final int DEFAULT_DELEGATED_PROXY_LIFETIME = 12*3600;

    public String getType() {
        return "VOMSMyProxy";
    }

    public Class getSecurityCredentialClass() {
        return VOMSMyProxySecurityCredential.class;
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UOr(new Usage[]{
                        new UAnd(new Usage[]{
                                new UOr(new Usage[]{
                                        new UFile(USAGE_INIT_PKCS12, VOMSContext.USERCERTKEY),
                                        new UAnd(USAGE_INIT_PEM, new Usage[]{new UFile(Context.USERCERT), new UFile(Context.USERKEY)})
                                }),
                                new UFilePath(Context.USERPROXY), new UHidden(Context.USERPASS),
                                new U(Context.SERVER), new U(Context.USERVO), new UOptional(VOMSContext.USERFQAN),
                                new UDuration(Context.LIFETIME) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        return (value!=null ? super.throwExceptionIfInvalid(value) : null);
                                    }
                                },
                                new UOptional(VOMSContext.DELEGATION) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        if (super.throwExceptionIfInvalid(value) != null) {
                                            String v = (String) value;
                                            if (!v.equalsIgnoreCase("none") && !v.equalsIgnoreCase("limited") && !v.equalsIgnoreCase("full")) {
                                                throw new BadParameterException("Expected: none | limited | full");
                                            }
                                        }
                                        return value;
                                    }
                                },
                                new UOptional(VOMSContext.PROXYTYPE) {
                                    protected Object throwExceptionIfInvalid(Object value) throws Exception {
                                        if (super.throwExceptionIfInvalid(value) != null) {
                                            String v = (String) value;
                                            if (!v.equalsIgnoreCase("old") && !v.equalsIgnoreCase("globus") && !v.equalsIgnoreCase("RFC820")) {
                                                throw new BadParameterException("Expected: old | globus | RFC820");
                                            }
                                        }
                                        return value;
                                    }
                                }
                        }),

                        // get delegated proxy from server
                        new UAnd(USAGE_GET_DELEGATED_MEMORY, new Usage[]{
                                new UNoPrompt(VOMSContext.USERPROXYOBJECT),
                                new UDuration(VOMSContext.DELEGATIONLIFETIME)
                        }),
                        new UAnd(USAGE_GET_DELEGATED_LOAD, new Usage[]{
                                new UFile(Context.USERPROXY),
                                new UDuration(VOMSContext.DELEGATIONLIFETIME)
                        }),

                        // local proxy
                        new UNoPrompt(USAGE_MEMORY, VOMSContext.USERPROXYOBJECT),
                        new UFile(USAGE_LOAD, Context.USERPROXY)
                }),
                new UFile(Context.CERTREPOSITORY),
                new UFile(VOMSContext.VOMSDIR),
                new U(VOMSContext.MYPROXYSERVER),
                new UOptional(VOMSContext.MYPROXYUSERID),
                new UOptional(VOMSContext.MYPROXYPASS),
        });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return super.getDefaults(attributes);
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, TimeoutException, NoSuccessException {
        try {
            switch(usage) {
                case USAGE_INIT_PKCS12:
                case USAGE_INIT_PEM:
                {
                    // create local temporary proxy
                    //String tempFile = File.createTempFile("vomsmyproxy", "txt").getAbsolutePath();
                    //attributes.put(Context.USERPROXY, tempFile);
                    //String oldLifeTime = (String) attributes.put(Context.LIFETIME, "PT12H");
                    VOMSSecurityCredential adaptor = (VOMSSecurityCredential) super.createSecurityCredential(usage, attributes, contextId);
                    //attributes.put(Context.LIFETIME, oldLifeTime);
                    GSSCredential cred = adaptor.getGSSCredential();

                    // send it to MyProxy server
                    storeCredential(cred, attributes);

                    // destroy local temporary proxy (requires anonymous to be authorized by server's default trusted_retrievers policy)
                    //Util.destroy(tempFile);

                    // returns
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_MEMORY:
                case USAGE_LOAD:
                {
                    // creates a VOMSMyProxySecurityCredential
                    return super.createSecurityCredential(usage, attributes, contextId);
                }
                case USAGE_GET_DELEGATED_MEMORY:
                {
                    // get old proxy (required unless anonymous is authorized by server's default trusted_retrievers policy)
                    GSSCredential oldCred = InMemoryProxySecurityCredential.toGSSCredential((String) attributes.get(VOMSContext.USERPROXYOBJECT));

                    // get delegated proxy
                    GSSCredential cred = getDelegatedCredential(oldCred, attributes);
//                    GSSCredential resignedCred = new VOMSProxyFactory(attributes, cred).createProxy();
                    return this.createSecurityAdaptor(cred, attributes);
                }
                case USAGE_GET_DELEGATED_LOAD:
                {
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
        } catch(IncorrectStateException e) {
            throw e;
        } catch(NoSuccessException e) {
            throw e;
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }
    protected SecurityCredential createSecurityAdaptor(GSSCredential cred, Map attributes) {
        File certRepository = new File((String) attributes.get(Context.CERTREPOSITORY));
        return new VOMSMyProxySecurityCredential(cred, certRepository);
    }

    public void destroySecurityAdaptor(Map attributes, String contextId) throws Exception {
        // get attributes
        GSSCredential cred = load(new File((String) attributes.get(Context.USERPROXY)));
        DestroyParams proxyParameters = new DestroyParams();

        String myProxyServer = (String) attributes.get(VOMSContext.MYPROXYSERVER);

    	String myProxyUserId = getUserName(cred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(VOMSContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(VOMSContext.MYPROXYPASS));
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

    private static void storeCredential(GSSCredential newCred, Map attributes) throws ParseException, MyProxyException {
        InitParams proxyParameters = new InitParams();

    	String myProxyUserId = getUserName(newCred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(VOMSContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(VOMSContext.MYPROXYPASS));
        }
        
        int storedLifetime = attributes.containsKey(Context.LIFETIME)
                ? UDuration.toInt(attributes.get(Context.LIFETIME))
                : DEFAULT_STORED_PROXY_LIFETIME;  // default lifetime for stored proxies
        proxyParameters.setLifetime(storedLifetime);
        
        MyProxy server = getServer((String) attributes.get(VOMSContext.MYPROXYSERVER));
        server.put(newCred, proxyParameters);

    }
    private static GSSCredential getDelegatedCredential(GSSCredential oldCred, Map attributes) throws ParseException, URISyntaxException, MyProxyException {
        GetParams proxyParameters = new GetParams();

    	String myProxyUserId = getUserName(oldCred, attributes);
        proxyParameters.setUserName(myProxyUserId);

        if (attributes.get(VOMSContext.MYPROXYPASS) != null) {
        	proxyParameters.setPassphrase((String)attributes.get(VOMSContext.MYPROXYPASS));
        }

        int delegatedLifetime = attributes.containsKey(VOMSContext.DELEGATIONLIFETIME)
                ? UDuration.toInt(attributes.get(VOMSContext.DELEGATIONLIFETIME))
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
        			: ((GlobusGSSCredentialImpl)cred).getGlobusCredential().getIdentity();
    }
}
